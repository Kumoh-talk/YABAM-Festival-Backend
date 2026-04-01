package domain.pos.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossConfirmResult {
	private final String tossPaymentKey;
	private final String tossOrderId;
	private final Integer amount;
	private final PaymentStatus status;
	private final String paymentMethod;
	private final LocalDateTime approvedAt;

	public Payment toPayment(UUID receiptId) {
		return Payment.builder()
			.receiptId(receiptId)
			.tossPaymentKey(tossPaymentKey)
			.tossOrderId(tossOrderId)
			.amount(amount)
			.status(status)
			.paymentMethod(paymentMethod)
			.approvedAt(approvedAt)
			.build();
	}
}
