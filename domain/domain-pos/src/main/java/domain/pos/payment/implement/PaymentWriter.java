package domain.pos.payment.implement;

import org.springframework.stereotype.Component;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentWriter {

    private final PaymentRepository paymentRepository;

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment updateStatus(Long paymentId, PaymentStatus status) {
        return paymentRepository.updateStatus(paymentId, status);
    }
}
