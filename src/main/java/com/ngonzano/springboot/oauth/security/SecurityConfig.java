package com.ngonzano.springboot.oauth.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.ngonzano.springboot.oauth.config.OauthProperties;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        AuthenticationManager authenticationManager(
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return new ProviderManager(provider);
        }

        @Bean
        JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
                return new NimbusJwtEncoder(jwkSource);
        }

        @Bean
        @Order(1)
        SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

                http
                                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                                .with(authorizationServerConfigurer,
                                                authorizationServer -> authorizationServer
                                                                .oidc(Customizer.withDefaults()))
                                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                                .exceptionHandling(exceptions -> exceptions
                                                .defaultAuthenticationEntryPointFor(
                                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
                return http.build();
        }

        @Bean
        @Order(2)
        SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/login", "/error", "/api/auth/login").permitAll()
                                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf.disable())
                                .formLogin(Customizer.withDefaults());
                return http.build();
        }

        @Bean
        @ConditionalOnMissingBean(RegisteredClientRepository.class)
        RegisteredClientRepository registeredClientRepository(OauthProperties oauthProperties) {
                OauthProperties.Client gateway = oauthProperties.getClient();
                OauthProperties.FlutterClient flutter = oauthProperties.getFlutter().getClient();

                RegisteredClient.Builder gatewayBuilder = RegisteredClient.withId(gateway.getRegistrationId())
                                .clientId(gateway.getId())
                                .clientSecret(gateway.getSecret())
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri(gateway.getRedirectUri())
                                .postLogoutRedirectUri(gateway.getPostLogoutRedirectUri())
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build());

                if (gateway.getRedirectUriAuth() != null && !gateway.getRedirectUriAuth().isBlank()) {
                        gatewayBuilder.redirectUri(gateway.getRedirectUriAuth());
                }

                RegisteredClient gatewayClient = gatewayBuilder.build();

                RegisteredClient.Builder flutterBuilder = RegisteredClient.withId(flutter.getRegistrationId())
                                .clientId(flutter.getId())
                                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                .clientSettings(ClientSettings.builder()
                                                .requireProofKey(true)
                                                .requireAuthorizationConsent(false)
                                                .build());

                if (flutter.getRedirectUri() != null && !flutter.getRedirectUri().isBlank()) {
                        flutterBuilder.redirectUri(flutter.getRedirectUri());
                }

                return new InMemoryRegisteredClientRepository(gatewayClient, flutterBuilder.build());
        }
}
