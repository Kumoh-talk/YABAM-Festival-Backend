package com.auth.infra.oidc.property;

import java.util.List;

public interface OidcClientProperties {
	String getJwksUri();

	List<String> getSecrets();

	String getIssuer();
}
