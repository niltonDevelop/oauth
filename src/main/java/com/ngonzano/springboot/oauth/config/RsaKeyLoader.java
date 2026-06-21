package com.ngonzano.springboot.oauth.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class RsaKeyLoader {

	private RsaKeyLoader() {
	}

	static KeyPair loadKeyPair(OauthProperties.Jwk jwk) {
		try {
			RSAPrivateKey privateKey = loadPrivateKey(jwk.getPrivateKey());
			RSAPublicKey publicKey = loadPublicKey(jwk.getPublicKey());
			return new KeyPair(publicKey, privateKey);
		} catch (Exception ex) {
			throw new IllegalStateException("No se pudieron cargar las claves RSA configuradas", ex);
		}
	}

	static KeyPair generateEphemeralKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException("No se pudo generar el par de claves RSA", ex);
		}
	}

	private static RSAPrivateKey loadPrivateKey(String pem) throws Exception {
		byte[] decoded = decodePem(pem);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
	}

	private static RSAPublicKey loadPublicKey(String pem) throws Exception {
		byte[] decoded = decodePem(pem);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
	}

	private static byte[] decodePem(String pem) {
		String normalized = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replace("-----BEGIN RSA PRIVATE KEY-----", "")
				.replace("-----END RSA PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(normalized.getBytes(StandardCharsets.UTF_8));
	}
}
