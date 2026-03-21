package domain.pos.payment.port.required;

import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;

public interface TossPaymentPort {

    TossConfirmResult confirm(String paymentKey, String orderId, Integer amount);

    /**
     * 결제 취소. cancelAmount가 null이면 전액 취소.
     * @return 취소 후 결제 상태 (CANCELED or PARTIAL_CANCELED)
     */
    PaymentStatus cancel(String paymentKey, String cancelReason, Integer cancelAmount);

    void verifyWebhookSignature(String rawBody, String signature);
}
