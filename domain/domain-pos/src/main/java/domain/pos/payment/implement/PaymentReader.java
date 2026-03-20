package domain.pos.payment.implement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReader {

    private final PaymentRepository paymentRepository;

    public Optional<Payment> findByReceiptId(UUID receiptId) {
        return paymentRepository.findByReceiptId(receiptId);
    }

    public Optional<Payment> findByTossPaymentKey(String tossPaymentKey) {
        return paymentRepository.findByTossPaymentKey(tossPaymentKey);
    }

    public Payment getByTossPaymentKey(String tossPaymentKey) {
        return paymentRepository.findByTossPaymentKey(tossPaymentKey)
            .orElseThrow(() -> {
                log.warn("결제 정보를 찾을 수 없습니다. tossPaymentKey={}", tossPaymentKey);
                return new ServiceException(ErrorCode.PAYMENT_NOT_FOUND);
            });
    }
}
