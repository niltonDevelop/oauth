package com.ngonzano.springboot.oauth.dto;

public record NativeTokenResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		String scope) {
}
