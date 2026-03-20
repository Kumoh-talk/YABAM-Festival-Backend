package com.pos.fixtures.payment;

import java.time.LocalDateTime;

import com.pos.payment.entity.PaymentEntity;
import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.payment.entity.PaymentStatus;

public class PaymentEntityFixture {

	private static final String TOSS_PAYMENT_KEY = "toss_payment_key_test_1234567890";
	private static final Integer AMOUNT = 10000;
	private static final String PAYMENT_METHOD = "카드";
	private static final LocalDateTime APPROVED_AT = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

	public static PaymentEntity GENERAL_DONE_PAYMENT_ENTITY(ReceiptEntity receiptEntity) {
		return PaymentEntity.builder()
			.receipt(receiptEntity)
			.tossPaymentKey(TOSS_PAYMENT_KEY)
			.tossOrderId(receiptEntity.getId().toString())
			.amount(AMOUNT)
			.status(PaymentStatus.DONE)
			.paymentMethod(PAYMENT_METHOD)
			.approvedAt(APPROVED_AT)
			.build();
	}

	public static PaymentEntity CUSTOM_PAYMENT_ENTITY(ReceiptEntity receiptEntity, String tossPaymentKey,
		Integer amount) {
		return PaymentEntity.builder()
			.receipt(receiptEntity)
			.tossPaymentKey(tossPaymentKey)
			.tossOrderId(receiptEntity.getId().toString())
			.amount(amount)
			.status(PaymentStatus.DONE)
			.paymentMethod(PAYMENT_METHOD)
			.approvedAt(APPROVED_AT)
			.build();
	}
}
