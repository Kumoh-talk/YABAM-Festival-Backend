package domain.pos.payment.port.required;

import domain.pos.payment.entity.TossConfirmResult;

public interface TossPaymentPort {

    TossConfirmResult confirm(String paymentKey, String orderId, Integer amount);

    void cancel(String paymentKey, String cancelReason);
}
