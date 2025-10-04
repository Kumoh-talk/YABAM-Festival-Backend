package fixtures.receipt.v2;

import java.time.LocalDateTime;

import domain.pos.receipt.entity.v2.domain.UsageTime;

public class UsageTimeFixture {
	public static LocalDateTime STARTED_TIME = LocalDateTime.of(2025, 10, 1, 17, 0);
	public static LocalDateTime STOPPED_TIME = LocalDateTime.of(2025, 10, 1, 19, 0);

	public static UsageTime NULL_USAGE_TIME() {
		return UsageTime.fromInfra(
			null,
			null);
	}

	public static UsageTime STARTED_USAGE_TIME() {
		return UsageTime.fromInfra(
			STARTED_TIME,
			null);
	}

	public static UsageTime STOPPED_USAGE_TIME() {
		return UsageTime.fromInfra(
			STARTED_TIME,
			STOPPED_TIME);
	}
}
