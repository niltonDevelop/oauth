package com.ngonzano.springboot.oauth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ngonzano.springboot.oauth.client.dto.UserAuthResponse;

@FeignClient(name = "msvc-users")
public interface MsvcUsersClient {

	@GetMapping("/internal/auth/users/{username}")
	UserAuthResponse findByUsername(
			@PathVariable("username") String username,
			@RequestHeader("X-Internal-Token") String internalToken);
}
