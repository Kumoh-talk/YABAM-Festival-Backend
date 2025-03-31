package com.auth.domain.entity;

public record OidcPayload(
	/* issuer */
	String iss,
	/* client id */
	String aud,
	/* aouth provider account unique id */
	String sub,
	String email
) {
}
