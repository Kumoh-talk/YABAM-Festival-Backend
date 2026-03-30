package domain.pos.payment.implement;

import domain.pos.payment.entity.Payment;
import domain.pos.receipt.entity.Receipt;

/**
 * {@link PaymentProcessor#validateAndPreempt} 반환값.
 * 선점 레코드 Payment와 잠금 조회된 영수증을 함께 전달한다.
 */
public record PreemptionResult(Payment preemptionPayment, Receipt receipt) {
}
