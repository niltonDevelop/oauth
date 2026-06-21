package com.ngonzano.springboot.oauth.security;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import com.ngonzano.springboot.oauth.config.OauthProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class AuthorizationServerConfig {

	@Bean
	@ConditionalOnMissingBean(JWKSource.class)
	JWKSource<SecurityContext> jwkSource(KeyPair rsaKeyPair, OauthProperties oauthProperties) {
		RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(oauthProperties.getJwk().getKeyId())
				.build();
		return new ImmutableJWKSet<>(new JWKSet(rsaKey));
	}

	@Bean
	@ConditionalOnMissingBean(JwtDecoder.class)
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	@ConditionalOnMissingBean(AuthorizationServerSettings.class)
	AuthorizationServerSettings authorizationServerSettings(OauthProperties oauthProperties) {
		return AuthorizationServerSettings.builder()
				.issuer(oauthProperties.getIssuer())
				.build();
	}
}
