package com.auth.infra.oidc.property.kakao;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.auth.infra.oidc.property.OidcClientProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.kakao")
public class KakaoOidcProperties implements OidcClientProperties {
	private final String jwksUri;
	private final List<String> secret;

	@Override
	public List<String> getSecrets() {
		return secret;
	}

	@Override
	public String getIssuer() {
		return jwksUri;
	}
}

