package domain.pos.payment.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.implement.PaymentProcessor;
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
import domain.pos.payment.implement.PreemptionResult;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.implement.TableWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TossPaymentPort tossPaymentPort;
	private final PaymentProcessor paymentProcessor;
	private final PaymentReader paymentReader;
	private final PaymentWriter paymentWriter;
	private final ReceiptReader receiptReader;
	private final ReceiptWriter receiptWriter;
	private final StoreValidator storeValidator;
	private final TableWriter tableWriter;

	/**
	 * 토스페이먼츠 결제 승인 및 영수증 자동 정산.
	 *
	 * <p>비관적 잠금과 선점 레코드 패턴으로 동시 결제 요청을 방지하며,
	 * 트랜잭션 범위를 외부 API 호출 전후로 분리해 DB 커넥션 점유 시간을 최소화한다.
	 * <ol>
	 *   <li>검증·잠금·선점 레코드 저장 (write 트랜잭션 → 커밋 후 잠금·커넥션 해제)</li>
	 *   <li>외부 API 호출 (트랜잭션 없음)</li>
	 *   <li>최종 저장·정산 (REQUIRES_NEW 트랜잭션)</li>
	 * </ol>
	 * PG 호출 또는 저장 실패 시 선점 레코드를 ABORTED로 마킹하고,
	 * Toss 결제가 DONE 상태라면 즉시 보상 취소를 시도한다.
	 */
	public Payment confirmPayment(String paymentKey, String orderId, Integer amount) {
		UUID receiptId = parseReceiptId(orderId);

		// ① 검증·잠금·선점 레코드 저장 — write 트랜잭션, 커밋 후 잠금·커넥션 해제
		PreemptionResult preemptionResult = paymentProcessor.validateAndPreempt(receiptId, paymentKey,
			orderId, amount);
		Long preemptionPaymentId = preemptionResult.preemptionPayment().getPaymentId();

		// ② 외부 API 호출 — 트랜잭션 없음
		TossConfirmResult result;
		try {
			result = tossPaymentPort.confirm(paymentKey, orderId, amount);
		} catch (ServiceException e) {
			if (e.getErrorCode() == ErrorCode.PAYMENT_CONFIRM_TIMEOUT) {
				// 타임아웃 소진 — IN_PROGRESS 유지, 스케줄러가 PG 상태 동기화
				log.warn("PG 타임아웃 소진. 선점 레코드 IN_PROGRESS 유지. 스케줄러 복구 대기. paymentId={}", preemptionPaymentId, e);
				throw new ServiceException(ErrorCode.PAYMENT_IN_PROGRESS);
			}
			// 금액 불일치·기타 PG 오류 — 즉시 ABORTED
			log.error("PG 결제 승인 실패. 선점 레코드 ABORTED 처리. paymentId={}", preemptionPaymentId, e);
			markPreemptionAborted(preemptionPaymentId, paymentKey);
			throw e;
		} catch (RuntimeException e) {
			log.error("PG 결제 승인 실패. 선점 레코드 ABORTED 처리. paymentId={}", preemptionPaymentId, e);
			markPreemptionAborted(preemptionPaymentId, paymentKey);
			throw e;
		}

		// ③ 최종 저장·정산 — 별도 트랜잭션, 실패 시 보상 취소
		try {
			return paymentProcessor.finalizeAndSettle(preemptionPaymentId, result,
				preemptionResult.receipt());
		} catch (Exception e) {
			if (result.getStatus() == PaymentStatus.DONE) {
				log.error("결제 저장 실패. 보상 취소 시도. paymentKey={}", paymentKey, e);
				try {
					tossPaymentPort.cancel(paymentKey, "결제 데이터 저장 실패로 인한 자동 취소", null);
					log.info("보상 취소 완료. paymentKey={}", paymentKey);
				} catch (Exception cancelEx) {
					log.error("보상 취소도 실패. 수동 처리 필요. paymentKey={}", paymentKey, cancelEx);
				}
			}
			markPreemptionAborted(preemptionPaymentId, paymentKey);

			throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
		}
	}

	private void markPreemptionAborted(Long preemptionPaymentId, String paymentKey) {
		try {
			paymentWriter.updateStatus(preemptionPaymentId, PaymentStatus.ABORTED);
			log.info("선점 레코드 ABORTED 처리 완료. paymentId={}", preemptionPaymentId);
		} catch (Exception e) {
			log.error("선점 레코드 ABORTED 처리 실패. 수동 처리 필요. paymentId={}, paymentKey={}",
				preemptionPaymentId, paymentKey, e);
		}
	}

	/**
	 * 결제 취소 (점주). cancelAmount가 null이면 전액 취소.
	 */
	@Transactional
	public void cancelPayment(String paymentKey, String cancelReason, Integer cancelAmount,
		UserPassport ownerPassport) {
		Payment payment = paymentReader.getByTossPaymentKey(paymentKey);

		if (payment.getStatus() == PaymentStatus.CANCELED) {
			log.warn("이미 취소된 결제입니다. paymentKey={}", paymentKey);
			throw new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
		}

		Receipt receipt = receiptReader.getReceiptWithTableAndStore(payment.getReceiptId())
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));

		storeValidator.validateStoreOwner(ownerPassport, receipt.getSale().getStore());

		PaymentStatus resultStatus = tossPaymentPort.cancel(paymentKey, cancelReason, cancelAmount);
		paymentWriter.updateStatus(payment.getPaymentId(), resultStatus);

		log.info("토스페이먼츠 결제 취소 완료. paymentKey={}, status={}", paymentKey, resultStatus);
	}

	/**
	 * 토스페이먼츠 웹훅 처리 (비동기 상태 동기화)
	 */
	@Transactional
	public void processWebhook(String paymentKey, String tossStatus) {
		paymentReader.findByTossPaymentKey(paymentKey).ifPresentOrElse(
			payment -> syncPaymentStatus(payment, tossStatus),
			() -> log.warn("웹훅 수신: 로컬 결제 정보 없음. paymentKey={}, status={}", paymentKey, tossStatus)
		);
	}

	/**
	 * 영수증 결제 정보 조회
	 */
	public Optional<Payment> findPaymentByReceiptId(UUID receiptId) {
		return paymentReader.findByReceiptId(receiptId);
	}

	/**
	 * 영업별 결제 목록 조회 (점주 정산 확인 용)
	 */
	public List<Payment> findPaymentsBySaleId(Long saleId) {
		return paymentReader.findBySaleId(saleId);
	}

	/**
	 * 토스페이먼츠 실시간 결제 상태 조회 (점주 reconciliation 용)
	 */
	public TossConfirmResult getPaymentFromToss(String paymentKey) {
		return tossPaymentPort.getPayment(paymentKey);
	}

	private void syncPaymentStatus(Payment payment, String tossStatus) {
		PaymentStatus newStatus;
		try {
			newStatus = PaymentStatus.valueOf(tossStatus);
		} catch (IllegalArgumentException e) {
			log.warn("웹훅 수신: 알 수 없는 상태값. paymentKey={}, status={}", payment.getTossPaymentKey(), tossStatus);
			return;
		}

		if (payment.getStatus() == newStatus) {
			return;
		}

		if (newStatus == PaymentStatus.CANCELED || newStatus == PaymentStatus.PARTIAL_CANCELED) {
			paymentWriter.updateStatus(payment.getPaymentId(), newStatus);
			log.info("웹훅 결제 상태 동기화 완료. paymentKey={}, status={}", payment.getTossPaymentKey(), newStatus);
		} else if (newStatus == PaymentStatus.DONE
			&& payment.getStatus() == PaymentStatus.WAITING_FOR_DEPOSIT) {
			paymentWriter.updateStatus(payment.getPaymentId(), newStatus);
			settleReceiptForVirtualAccount(payment);
		} else {
			log.debug("웹훅 수신: 처리 대상 아닌 상태값 무시. paymentKey={}, status={}",
				payment.getTossPaymentKey(), newStatus);
		}
	}

	private void settleReceiptForVirtualAccount(Payment payment) {
		receiptReader.getReceiptWithTableAndStore(payment.getReceiptId()).ifPresentOrElse(
			receipt -> {
				if (!receipt.getReceiptInfo().isAdjustment()) {
					tableWriter.changeTableActiveStatus(false, receipt.getTable());
					receiptWriter.adjustReceipts(List.of(receipt));
					log.info("웹훅 가상계좌 입금 완료 처리. paymentKey={}, receiptId={}",
						payment.getTossPaymentKey(), payment.getReceiptId());
				}
			},
			() -> log.warn("웹훅 가상계좌 입금 완료: 영수증을 찾을 수 없음. receiptId={}",
				payment.getReceiptId())
		);
	}

	private UUID parseReceiptId(String orderId) {
		try {
			return UUID.fromString(orderId);
		} catch (IllegalArgumentException e) {
			log.warn("orderId가 유효한 UUID 형식이 아닙니다. orderId={}", orderId);
			throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
