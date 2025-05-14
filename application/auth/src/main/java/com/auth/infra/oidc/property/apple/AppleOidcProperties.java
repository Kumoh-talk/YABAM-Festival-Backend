package com.auth.infra.oidc.property.apple;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.auth.infra.oidc.property.OidcClientProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.apple")
public class AppleOidcProperties implements OidcClientProperties {
	private final String jwksUri;
	private final String secret;

	@Override
	public List<String> getSecrets() {
		return List.of(secret);
	}

	@Override
	public String getIssuer() {
		return jwksUri;
	}
}

