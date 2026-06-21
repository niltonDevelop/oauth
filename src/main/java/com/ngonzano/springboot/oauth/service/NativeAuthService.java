package com.ngonzano.springboot.oauth.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ngonzano.springboot.oauth.config.OauthProperties;
import com.ngonzano.springboot.oauth.dto.LoginRequest;
import com.ngonzano.springboot.oauth.dto.NativeTokenResponse;

@Service
public class NativeAuthService {

	private static final long TOKEN_TTL_SECONDS = 3600L;

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final String audience;

	public NativeAuthService(
			AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			OauthProperties oauthProperties) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.issuer = oauthProperties.getIssuer();
		this.audience = oauthProperties.getFlutter().getClient().getId();
	}

	public NativeTokenResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.username(),
						request.password()));

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(TOKEN_TTL_SECONDS);

		List<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(authentication.getName())
				.audience(List.of(audience))
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("scope", "openid profile")
				.claim("roles", roles)
				.build();

		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return new NativeTokenResponse(accessToken, "Bearer", TOKEN_TTL_SECONDS, "openid profile");
	}
}
