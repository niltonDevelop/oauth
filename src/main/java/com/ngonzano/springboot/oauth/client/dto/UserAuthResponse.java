package com.ngonzano.springboot.oauth.client.dto;

import java.util.List;

public record UserAuthResponse(
		String username,
		String password,
		boolean enabled,
		List<String> roles) {
}
