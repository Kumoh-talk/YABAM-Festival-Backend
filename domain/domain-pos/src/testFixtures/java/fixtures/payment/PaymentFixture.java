package fixtures.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;

public class PaymentFixture {

	public static final Long GENERAL_PAYMENT_ID = 1L;
	public static final UUID GENERAL_RECEIPT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	public static final String GENERAL_TOSS_PAYMENT_KEY = "toss_payment_key_test_1234567890";
	public static final String GENERAL_TOSS_ORDER_ID = GENERAL_RECEIPT_ID.toString();
	public static final Integer GENERAL_AMOUNT = 10000;
	public static final String GENERAL_PAYMENT_METHOD = "카드";
	public static final LocalDateTime GENERAL_APPROVED_AT = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

	public static Payment GENERAL_DONE_PAYMENT() {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(GENERAL_RECEIPT_ID)
			.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
			.tossOrderId(GENERAL_TOSS_ORDER_ID)
			.amount(GENERAL_AMOUNT)
			.status(PaymentStatus.DONE)
			.paymentMethod(GENERAL_PAYMENT_METHOD)
			.approvedAt(GENERAL_APPROVED_AT)
			.build();
	}

	public static Payment GENERAL_CANCELED_PAYMENT() {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(GENERAL_RECEIPT_ID)
			.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
			.tossOrderId(GENERAL_TOSS_ORDER_ID)
			.amount(GENERAL_AMOUNT)
			.status(PaymentStatus.CANCELED)
			.paymentMethod(GENERAL_PAYMENT_METHOD)
			.approvedAt(GENERAL_APPROVED_AT)
			.build();
	}

	public static Payment GENERAL_PARTIAL_CANCELED_PAYMENT() {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(GENERAL_RECEIPT_ID)
			.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
			.tossOrderId(GENERAL_TOSS_ORDER_ID)
			.amount(GENERAL_AMOUNT)
			.status(PaymentStatus.PARTIAL_CANCELED)
			.paymentMethod(GENERAL_PAYMENT_METHOD)
			.approvedAt(GENERAL_APPROVED_AT)
			.build();
	}

	public static Payment GENERAL_IN_PROGRESS_PAYMENT() {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(GENERAL_RECEIPT_ID)
			.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
			.tossOrderId(GENERAL_TOSS_ORDER_ID)
			.amount(GENERAL_AMOUNT)
			.status(PaymentStatus.IN_PROGRESS)
			.paymentMethod(null)
			.approvedAt(null)
			.build();
	}

	public static Payment GENERAL_WAITING_FOR_DEPOSIT_PAYMENT() {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(GENERAL_RECEIPT_ID)
			.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
			.tossOrderId(GENERAL_TOSS_ORDER_ID)
			.amount(GENERAL_AMOUNT)
			.status(PaymentStatus.WAITING_FOR_DEPOSIT)
			.paymentMethod("가상계좌")
			.approvedAt(null)
			.build();
	}

	public static Payment CUSTOM_PAYMENT(UUID receiptId, String tossPaymentKey, Integer amount) {
		return Payment.builder()
			.paymentId(GENERAL_PAYMENT_ID)
			.receiptId(receiptId)
			.tossPaymentKey(tossPaymentKey)
			.tossOrderId(receiptId.toString())
			.amount(amount)
			.status(PaymentStatus.DONE)
			.paymentMethod(GENERAL_PAYMENT_METHOD)
			.approvedAt(GENERAL_APPROVED_AT)
			.build();
	}
}
