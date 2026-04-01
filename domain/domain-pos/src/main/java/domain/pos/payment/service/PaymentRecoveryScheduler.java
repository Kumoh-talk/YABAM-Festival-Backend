package domain.pos.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.implement.PaymentProcessor;
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRecoveryScheduler {

	// 토스 권장 ReadTimeout(30s) × confirmMaxRetries(1) = 30s 이상이면 @Retryable 처리가 완료된 것으로 판단.
	// 5분은 해당 기준의 충분한 여유값이며, 레이스 컨디션(아직 처리 중인 선점 레코드 오인 복구)을 방지한다.
	private static final int IN_PROGRESS_TIMEOUT_MINUTES = 5;

	private final PaymentReader paymentReader;
	private final PaymentWriter paymentWriter;
	private final TossPaymentPort tossPaymentPort;
	private final PaymentProcessor paymentProcessor;
	private final ReceiptReader receiptReader;

	@Scheduled(fixedDelay = 60_000)
	public void recoverStuckPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(IN_PROGRESS_TIMEOUT_MINUTES);
		List<Payment> stuck = paymentReader.findInProgressOlderThan(threshold);

		if (stuck.isEmpty()) {
			return;
		}

		log.info("IN_PROGRESS 복구 대상 {}건 처리 시작", stuck.size());
		stuck.forEach(this::recoverSinglePayment);
	}

	private void recoverSinglePayment(Payment payment) {
		String paymentKey = payment.getTossPaymentKey();
		try {
			TossConfirmResult result = tossPaymentPort.getPayment(paymentKey);
			PaymentStatus tossStatus = result.getStatus();

			if (tossStatus == PaymentStatus.DONE || tossStatus == PaymentStatus.WAITING_FOR_DEPOSIT) {
				Receipt receipt = receiptReader.getReceiptWithTableAndStore(payment.getReceiptId())
					.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
				paymentProcessor.finalizeAndSettle(payment.getPaymentId(), result, receipt);
				log.info("IN_PROGRESS 복구 완료. paymentKey={}, status={}", paymentKey, tossStatus);
			} else {
				paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.ABORTED);
				log.warn("IN_PROGRESS 복구 불가. ABORTED 처리. paymentKey={}, tossStatus={}",
					paymentKey, tossStatus);
			}
		} catch (ServiceException e) {
			log.warn("IN_PROGRESS 복구 재시도 예정 (Toss 미응답). paymentKey={}", paymentKey, e);
		} catch (Exception e) {
			log.error("IN_PROGRESS 복구 중 예상치 못한 오류. paymentKey={}", paymentKey, e);
		}
	}
}
