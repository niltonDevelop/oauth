package com.ngonzano.springboot.oauth.dto;

public record LoginRequest(String username, String password) {

	public LoginRequest {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username is required");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("password is required");
		}
	}
}
