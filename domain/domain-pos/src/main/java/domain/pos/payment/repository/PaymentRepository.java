package domain.pos.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByReceiptId(UUID receiptId);

    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    Payment updateStatus(Long paymentId, PaymentStatus status);

    Payment updateConfirmResult(Long paymentId, PaymentStatus status, String paymentMethod,
        LocalDateTime approvedAt);

    void delete(Long paymentId);

    List<Payment> findBySaleId(Long saleId);

    List<Payment> findInProgressOlderThan(LocalDateTime threshold);
}
