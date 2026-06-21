package com.ngonzano.springboot.oauth.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ngonzano.springboot.oauth.client.MsvcUsersClient;
import com.ngonzano.springboot.oauth.client.dto.UserAuthResponse;

import feign.FeignException;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final MsvcUsersClient msvcUsersClient;
	private final String internalToken;

	public DatabaseUserDetailsService(
			MsvcUsersClient msvcUsersClient,
			@Value("${oauth.internal-token}") String internalToken) {
		this.msvcUsersClient = msvcUsersClient;
		this.internalToken = internalToken;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			UserAuthResponse authUser = msvcUsersClient.findByUsername(username, internalToken);
			if (authUser == null || !authUser.enabled()) {
				throw new UsernameNotFoundException("Usuario o contraseña incorrectos");
			}

			String encodedPassword = authUser.password();
			if (!encodedPassword.startsWith("{")) {
				encodedPassword = "{bcrypt}" + encodedPassword;
			}

			List<SimpleGrantedAuthority> authorities = authUser.roles().stream()
					.map(SimpleGrantedAuthority::new)
					.toList();

			return User.builder()
					.username(authUser.username())
					.password(encodedPassword)
					.authorities(authorities)
					.build();
		} catch (FeignException.NotFound ex) {
			throw new UsernameNotFoundException("Usuario o contraseña incorrectos");
		}
	}
}
