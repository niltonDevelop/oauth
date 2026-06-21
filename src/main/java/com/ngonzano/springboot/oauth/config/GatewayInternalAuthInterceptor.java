package com.ngonzano.springboot.oauth.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayInternalAuthInterceptor implements HandlerInterceptor {

	private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

	private final String internalToken;

	public GatewayInternalAuthInterceptor(OauthProperties oauthProperties) {
		this.internalToken = oauthProperties.getGateway().getInternalToken();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String token = request.getHeader(INTERNAL_TOKEN_HEADER);
		if (internalToken.equals(token)) {
			return true;
		}
		response.sendError(HttpStatus.FORBIDDEN.value(), "Acceso denegado");
		return false;
	}
}
