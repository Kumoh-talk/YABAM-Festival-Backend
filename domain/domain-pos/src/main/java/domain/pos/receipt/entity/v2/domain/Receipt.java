package domain.pos.receipt.entity.v2.domain;

import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Receipt {
	private UUID id;
	private UsageTime usageTime;
	private boolean isAdjustment;

	private Long saleId;
	private UUID tableId;

	public static final int UNIT_MINUTES = 60;
	public static final int FOUR_TABLE_COST = 4000;
	public static final int SIX_TABLE_COST = 6000;

	@Builder(access = AccessLevel.PRIVATE)
	private Receipt(UUID id, UsageTime usageTime, boolean isAdjustment, Long saleId, UUID tableId) {
		this.id = id;
		this.usageTime = usageTime;
		this.isAdjustment = isAdjustment;
		this.saleId = saleId;
		this.tableId = tableId;
	}

	public static Receipt create(Long saleId, UUID tableId) {
		return Receipt.builder()
			.id(null)
			.usageTime(UsageTime.startUse())
			.isAdjustment(false)
			.saleId(requireNonNull(saleId))
			.tableId(requireNonNull(tableId))
			.build();
	}

	public void stopUsage() {
		this.usageTime = this.usageTime.stopUse();
	}

	public void restartUsage() {
		if (this.isAdjustment) {
			throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
		this.usageTime = this.usageTime.restartUse();
	}

	public void adjust() {
		if (this.usageTime.getStop() == null) {
			throw new ServiceException(ErrorCode.NOT_STOPPED_RECEIPT);
		}

		if (this.isAdjustment) {
			throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
		this.isAdjustment = true;
	}

	public void moveTable(UUID moveTableId) {
		if (this.isAdjustment) {
			throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
		this.tableId = requireNonNull(moveTableId);
	}

	public void syncStartUsageTime(LocalDateTime startUsageTime) {
		if (this.isAdjustment) {
			throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
		this.usageTime = UsageTime.of(startUsageTime, this.usageTime.getStop());
	}

	public long calculateUnits() {
		return this.usageTime.units(UNIT_MINUTES);
	}

	public static Receipt fromInfra(UUID id, UsageTime usageTime, boolean isAdjustment, Long saleId, UUID tableId) {
		return Receipt.builder()
			.id(id)
			.usageTime(usageTime)
			.isAdjustment(isAdjustment)
			.saleId(saleId)
			.tableId(tableId)
			.build();
	}
}
