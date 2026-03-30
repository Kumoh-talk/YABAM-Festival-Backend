package domain.pos.payment.implement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.table.implement.TableWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 승인 트랜잭션을 외부 API 호출 전후로 분리하기 위한 컴포넌트.
 * <ul>
 *   <li>{@link #validateAndPreempt} — write 트랜잭션으로 비관적 잠금·검증·선점 레코드 저장 후 커밋</li>
 *   <li>{@link #finalizeAndSettle} — REQUIRES_NEW 트랜잭션으로 선점 레코드 업데이트·정산 처리</li>
 * </ul>
 * 동일 클래스 내 메서드 호출은 Spring AOP 프록시를 우회하므로 PaymentService와 분리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessor {

	private final PaymentReader paymentReader;
	private final PaymentWriter paymentWriter;
	private final ReceiptReader receiptReader;
	private final ReceiptWriter receiptWriter;
	private final TableWriter tableWriter;

	/**
	 * 비관적 잠금 검증 및 선점 레코드 저장 — write 트랜잭션 (커밋 후 잠금·커넥션 해제).
	 *
	 * <p>영수증 행에 PESSIMISTIC_WRITE 잠금을 획득해 동시 결제 요청을 직렬화한다.
	 * 검증 통과 후 {@link PaymentStatus#IN_PROGRESS} 선점 레코드를 저장하고 즉시 커밋한다.
	 * 이후 도착하는 중복 요청은 선점 레코드 존재를 감지해 {@code ALREADY_PAID_RECEIPT}로 실패한다.
	 *
	 * @return 선점 레코드 Payment와 잠금 조회된 영수증
	 */
	@Transactional
	public PreemptionResult validateAndPreempt(UUID receiptId, String paymentKey, String orderId,
		Integer amount) {
		Receipt receipt = receiptReader.getReceiptWithTableAndStoreAndLock(receiptId)
			.orElseThrow(() -> {
				log.warn("결제 승인 대상 영수증을 찾을 수 없습니다. receiptId={}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});

		ReceiptInfo receiptInfo = receipt.getReceiptInfo();

		if (receiptInfo.isAdjustment()) {
			log.warn("이미 정산된 영수증입니다. receiptId={}", receiptId);
			throw new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT);
		}

		Integer expectedAmount = receiptInfo.getOccupancyFee();
		if (expectedAmount != null && !expectedAmount.equals(amount)) {
			log.warn("결제 금액 불일치. expected={}, actual={}", expectedAmount, amount);
			throw new ServiceException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		Optional<Payment> existingPayment = paymentReader.findByReceiptId(receiptId);
		if (existingPayment.isPresent()) {
			Payment existing = existingPayment.get();
			if (existing.getStatus() != PaymentStatus.ABORTED) {
				log.warn("이미 결제된 영수증입니다. receiptId={}, paymentStatus={}",
					receiptId, existing.getStatus());
				throw new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT);
			}
			// 이전 실패(ABORTED) 선점 레코드 제거 후 재시도 허용
			log.info("이전 실패 선점 레코드 제거 후 재시도. receiptId={}, paymentId={}",
				receiptId, existing.getPaymentId());
			paymentWriter.delete(existing.getPaymentId());
		}

		Payment preemptionPayment = paymentWriter.save(Payment.builder()
			.receiptId(receiptId)
			.tossPaymentKey(paymentKey)
			.tossOrderId(orderId)
			.amount(amount)
			.status(PaymentStatus.IN_PROGRESS)
			.build());

		log.info("결제 선점 레코드 저장 완료. receiptId={}, paymentId={}", receiptId,
			preemptionPayment.getPaymentId());
		return new PreemptionResult(preemptionPayment, receipt);
	}

	/**
	 * 선점 레코드 최종 업데이트 및 영수증 정산 — 별도 트랜잭션 (외부 API 호출 후 실행).
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Payment finalizeAndSettle(Long preemptionPaymentId, TossConfirmResult result,
		Receipt receipt) {
		UUID receiptId = receipt.getReceiptInfo().getReceiptId();
		Payment payment = paymentWriter.updateConfirmResult(preemptionPaymentId, result.getStatus(),
			result.getPaymentMethod(), result.getApprovedAt());

		if (result.getStatus() == PaymentStatus.DONE) {
			tableWriter.changeTableActiveStatus(false, receipt.getTable());

			receiptWriter.adjustReceipts(List.of(receipt));
			log.info("결제 최종 저장·정산 완료. receiptId={}, paymentKey={}", receiptId,
				result.getTossPaymentKey());
		} else {
			log.info("결제 최종 저장 완료 (입금 대기). receiptId={}, paymentKey={}, status={}",
				receiptId, result.getTossPaymentKey(), result.getStatus());
		}

		return payment;
	}
}
