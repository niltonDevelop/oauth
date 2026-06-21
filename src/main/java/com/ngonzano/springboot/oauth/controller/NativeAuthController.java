package com.ngonzano.springboot.oauth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngonzano.springboot.oauth.dto.LoginRequest;
import com.ngonzano.springboot.oauth.dto.NativeTokenResponse;
import com.ngonzano.springboot.oauth.service.NativeAuthService;

@RestController
@RequestMapping("/api/auth")
public class NativeAuthController {

	private final NativeAuthService nativeAuthService;

	public NativeAuthController(NativeAuthService nativeAuthService) {
		this.nativeAuthService = nativeAuthService;
	}

	@PostMapping("/login")
	public NativeTokenResponse login(@RequestBody LoginRequest request) {
		return nativeAuthService.login(request);
	}

	@ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
	public ResponseEntity<Map<String, String>> handleInvalidCredentials(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "Usuario o contraseña incorrectos"));
	}
}
