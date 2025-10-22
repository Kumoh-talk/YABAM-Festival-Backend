package domain.pos.receipt.entity.v2.domain;

import static java.util.Objects.*;

import java.time.Duration;
import java.time.LocalDateTime;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import lombok.Value;

@Value
public class UsageTime {
	LocalDateTime start;
	LocalDateTime stop;

	private UsageTime(LocalDateTime start, LocalDateTime stop) {
		requireNonNull(start);
		if (stop != null && start.isAfter(stop)) {
			throw new IllegalArgumentException("start는 stop보다 이후일 수 없습니다.");
		}

		this.start = start;
		this.stop = stop;
	}

	public static UsageTime startUse() {
		return new UsageTime(LocalDateTime.now(), null);
	}

	public UsageTime restartUse() {
		return new UsageTime(this.start, null);
	}

	public UsageTime stopUse() {
		if (this.stop != null) {
			throw new ServiceException(ErrorCode.ALREADY_STOPPED_RECEIPT);
		}
		return new UsageTime(this.start, LocalDateTime.now());
	}

	public long units(int unitMinutes) {
		if (this.start == null || this.stop == null) {
			throw new IllegalStateException("점유 시간을 계산할 수 있는 상태가 아닙니다.");
		}
		if (unitMinutes <= 0) {
			throw new IllegalArgumentException("단위 분은 1 이상이어야 합니다.");
		}

		long minutes = Duration.between(start, stop).toMinutes();
		return Math.max(1, (minutes + unitMinutes - 1) / unitMinutes);
	}

	public static UsageTime of(LocalDateTime start, LocalDateTime stop) {
		return new UsageTime(start, stop);
	}
}
