package domain.pos.payment.repository;

import java.util.Optional;
import java.util.UUID;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByReceiptId(UUID receiptId);

    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    Payment updateStatus(Long paymentId, PaymentStatus status);
}
