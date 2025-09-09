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
	}

	public static AuditStamp create(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		return new AuditStamp(requireNonNull(createdAt), updatedAt, deletedAt);
	}

	public AuditStamp update(LocalDateTime updatedAt) {
		return new AuditStamp(this.createdAt, requireNonNull(updatedAt), this.deletedAt);
	}
}

