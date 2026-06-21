package com.ngonzano.springboot.oauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {

	@NotBlank
	private String issuer;

	@NotBlank
	private String internalToken;

	@Valid
	private final Admin admin = new Admin();

	@Valid
	private final Client client = new Client();

	@Valid
	private final Flutter flutter = new Flutter();

	@Valid
	private final Gateway gateway = new Gateway();

	@Valid
	private final Jwk jwk = new Jwk();

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getInternalToken() {
		return internalToken;
	}

	public void setInternalToken(String internalToken) {
		this.internalToken = internalToken;
	}

	public Admin getAdmin() {
		return admin;
	}

	public Client getClient() {
		return client;
	}

	public Flutter getFlutter() {
		return flutter;
	}

	public Gateway getGateway() {
		return gateway;
	}

	public Jwk getJwk() {
		return jwk;
	}

	public static class Admin {

		private String username;

		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class Client {

		@NotBlank
		private String registrationId;

		@NotBlank
		private String id;

		@NotBlank
		private String secret;

		@NotBlank
		private String redirectUri;

		private String redirectUriAuth;

		@NotBlank
		private String postLogoutRedirectUri;

		public String getRegistrationId() {
			return registrationId;
		}

		public void setRegistrationId(String registrationId) {
			this.registrationId = registrationId;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public void setRedirectUri(String redirectUri) {
			this.redirectUri = redirectUri;
		}

		public String getRedirectUriAuth() {
			return redirectUriAuth;
		}

		public void setRedirectUriAuth(String redirectUriAuth) {
			this.redirectUriAuth = redirectUriAuth;
		}

		public String getPostLogoutRedirectUri() {
			return postLogoutRedirectUri;
		}

		public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
			this.postLogoutRedirectUri = postLogoutRedirectUri;
		}
	}

	public static class Flutter {

		@Valid
		private final FlutterClient client = new FlutterClient();

		public FlutterClient getClient() {
			return client;
		}
	}

	public static class FlutterClient {

		@NotBlank
		private String registrationId;

		@NotBlank
		private String id;

		private String redirectUri;

		public String getRegistrationId() {
			return registrationId;
		}

		public void setRegistrationId(String registrationId) {
			this.registrationId = registrationId;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public void setRedirectUri(String redirectUri) {
			this.redirectUri = redirectUri;
		}
	}

	public static class Gateway {

		@NotBlank
		private String internalToken;

		public String getInternalToken() {
			return internalToken;
		}

		public void setInternalToken(String internalToken) {
			this.internalToken = internalToken;
		}
	}

	public static class Jwk {

		@NotBlank
		private String keyId;

		private String privateKey;

		private String publicKey;

		public String getKeyId() {
			return keyId;
		}

		public void setKeyId(String keyId) {
			this.keyId = keyId;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public void setPrivateKey(String privateKey) {
			this.privateKey = privateKey;
		}

		public String getPublicKey() {
			return publicKey;
		}

		public void setPublicKey(String publicKey) {
			this.publicKey = publicKey;
		}

		public boolean hasConfiguredKeyPair() {
			return isNotBlank(privateKey) && isNotBlank(publicKey);
		}

		private static boolean isNotBlank(String value) {
			return value != null && !value.isBlank();
		}
	}
}
