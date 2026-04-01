package com.pos.receipt;

import java.time.LocalDateTime;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.table.entity.TableEntity;

public class ReceiptEntityFixture {
	private static final LocalDateTime START_TIME = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
	private static final LocalDateTime END_TIME = LocalDateTime.of(2020, 1, 1, 1, 0, 0);
	private static final Integer OCCUPANCY_FEE = 1000;
	private static final boolean IS_ADJUSTMENT = true;

	public static ReceiptEntity GENERAL_ADJUSTMENT_RECEIPT(SaleEntity saleEntity, TableEntity tableEntity) {
		return ReceiptEntity.builder()
			.isAdjustment(IS_ADJUSTMENT)
			.startUsageTime(START_TIME)
			.stopUsageTime(END_TIME)
			.occupancyFee(OCCUPANCY_FEE)
			.sale(saleEntity)
			.table(tableEntity)
			.build();
	}

}
