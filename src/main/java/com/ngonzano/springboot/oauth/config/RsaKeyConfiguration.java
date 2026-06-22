package com.ngonzano.springboot.oauth.config;

import java.security.KeyPair;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
public class RsaKeyConfiguration {

	@Bean
	@Profile("prod")
	KeyPair prodRsaKeyPair(OauthProperties oauthProperties) {
		OauthProperties.Jwk jwk = oauthProperties.getJwk();
		if (!jwk.hasConfiguredKeyPair()) {
			throw new IllegalStateException(
					"Producción requiere oauth.jwk.private-key y oauth.jwk.public-key (PEM) vía variables de entorno");
		}
		return RsaKeyLoader.loadKeyPair(jwk);
	}

	@Bean
	@Profile("!prod")
	@ConditionalOnMissingBean(KeyPair.class)
	KeyPair devRsaKeyPair(OauthProperties oauthProperties, Environment environment) {
		OauthProperties.Jwk jwk = oauthProperties.getJwk();
		if (jwk.hasConfiguredKeyPair()) {
			return RsaKeyLoader.loadKeyPair(jwk);
		}
		if (environment.acceptsProfiles(Profiles.of("dev", "docker"))) {
			return RsaKeyLoader.generateEphemeralKeyPair();
		}
		throw new IllegalStateException(
				"Configura oauth.jwk.private-key y oauth.jwk.public-key o activa el perfil dev/docker");
	}
}
