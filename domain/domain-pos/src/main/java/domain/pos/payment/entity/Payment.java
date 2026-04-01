package domain.pos.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Payment {
	private final Long paymentId;
	private final UUID receiptId;
	private final String tossPaymentKey;
	private final String tossOrderId;
	private final Integer amount;
	private PaymentStatus status;
	private final String paymentMethod;
	private final LocalDateTime approvedAt;
	private final LocalDateTime createdAt;

	@Builder
	public Payment(Long paymentId, UUID receiptId, String tossPaymentKey, String tossOrderId,
		Integer amount, PaymentStatus status, String paymentMethod, LocalDateTime approvedAt,
		LocalDateTime createdAt) {
		this.paymentId = paymentId;
		this.receiptId = receiptId;
		this.tossPaymentKey = tossPaymentKey;
		this.tossOrderId = tossOrderId;
		this.amount = amount;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.approvedAt = approvedAt;
		this.createdAt = createdAt;
	}

}
