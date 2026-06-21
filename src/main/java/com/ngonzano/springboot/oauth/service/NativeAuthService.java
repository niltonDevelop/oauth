package com.ngonzano.springboot.oauth.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ngonzano.springboot.oauth.dto.LoginRequest;
import com.ngonzano.springboot.oauth.dto.NativeTokenResponse;

@Service
public class NativeAuthService {

	private static final long TOKEN_TTL_SECONDS = 300L;

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final String audience;

	public NativeAuthService(
			AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			@Value("${oauth.issuer}") String issuer,
			@Value("${oauth.flutter.client.id:flutter-app}") String audience) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.audience = audience;
	}

	public NativeTokenResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.username(),
						request.password()));

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(TOKEN_TTL_SECONDS);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(authentication.getName())
				.audience(List.of(audience))
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("scope", "openid profile")
				.build();

		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return new NativeTokenResponse(accessToken, "Bearer", TOKEN_TTL_SECONDS, "openid profile");
	}
}
