package com.auth.infra.oidc.property.google;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.auth.infra.oidc.property.OidcClientProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.google")
public class GoogleOidcProperties implements OidcClientProperties {
	private final String jwksUri;
	private final String secret;
	private final String issuer;

	@Override
	public List<String> getSecrets() {
		return List.of(secret);
	}
}
