package com.ngonzano.springboot.oauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GatewayInternalAuthWebConfig implements WebMvcConfigurer {

	private final GatewayInternalAuthInterceptor gatewayInternalAuthInterceptor;

	public GatewayInternalAuthWebConfig(GatewayInternalAuthInterceptor gatewayInternalAuthInterceptor) {
		this.gatewayInternalAuthInterceptor = gatewayInternalAuthInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(gatewayInternalAuthInterceptor)
				.addPathPatterns("/api/auth/**");
	}
}
