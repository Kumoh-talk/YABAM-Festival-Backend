package com.pos.fixtures.call;

import com.pos.call.entity.CallEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;

import domain.pos.call.entity.CallMessage;

public class CallEntityFixture {
	/* 기본 값 */
	private static final String MESSAGE = "직원 호출 요청입니다.";
	private static final Boolean IS_COMPLETE = false;

	/**
	 * 기본 CallEntity
	 */
	public static CallEntity GENERAL_CALL_ENTITY(ReceiptEntity receiptEntity, SaleEntity saleEntity) {
		return CallEntity.of(
			receiptEntity,
			saleEntity,
			CallMessage.of(MESSAGE, IS_COMPLETE)
		);
	}

}
