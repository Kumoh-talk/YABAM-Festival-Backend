package com.vo;

import static java.util.Objects.*;

import java.time.LocalDateTime;

public record AuditStamp(
	// TODO : 기준이 UTC로 항상 동일한 Instant 타입으로 변경?
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	LocalDateTime deletedAt
) {
	public AuditStamp {
		requireNonNull(createdAt);
	}

	public static AuditStamp create(LocalDateTime createdAt) {
		return new AuditStamp(createdAt, null, null);
	}

	public static AuditStamp update(LocalDateTime createdAt, LocalDateTime updatedAt) {
		return new AuditStamp(createdAt, requireNonNull(updatedAt), null);
	}

	public static AuditStamp delete(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		return new AuditStamp(createdAt, requireNonNull(updatedAt), requireNonNull(deletedAt));
	}
}

