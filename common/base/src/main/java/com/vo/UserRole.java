package com.vo;

public enum UserRole {
	ROLE_ANONYMOUS,
	ROLE_USER,
	ROLE_OWNER;

	public boolean isHigherOrEqual(UserRole other) {
		return this.ordinal() >= other.ordinal();
	}
}
