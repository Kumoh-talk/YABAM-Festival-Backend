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
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
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
    private final PaymentReader paymentReader;
    private final PaymentWriter paymentWriter;
    private final ReceiptReader receiptReader;
    private final ReceiptWriter receiptWriter;
    private final StoreValidator storeValidator;
    private final TableWriter tableWriter;

    /**
     * 토스페이먼츠 결제 승인 및 영수증 자동 정산
     */
    @Transactional
    public Payment confirmPayment(String paymentKey, String orderId, Integer amount) {
        UUID receiptId = parseReceiptId(orderId);

        Receipt receipt = receiptReader.getReceiptWithTableAndStore(receiptId)
            .orElseThrow(() -> {
                log.warn("결제 승인 대상 영수증을 찾을 수 없습니다. receiptId={}", receiptId);
                return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
            });

        ReceiptInfo receiptInfo = receipt.getReceiptInfo();

        if (receiptInfo.isAdjustment()) {
            log.warn("이미 정산된 영수증입니다. receiptId={}", receiptId);
            throw new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT);
        }

        if (paymentReader.findByReceiptId(receiptId).isPresent()) {
            log.warn("이미 결제된 영수증입니다. receiptId={}", receiptId);
            throw new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT);
        }

        Integer expectedAmount = receiptInfo.getOccupancyFee();
        if (expectedAmount != null && !expectedAmount.equals(amount)) {
            log.warn("결제 금액 불일치. expected={}, actual={}", expectedAmount, amount);
            throw new ServiceException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        TossConfirmResult result = tossPaymentPort.confirm(paymentKey, orderId, amount);
        Payment payment = paymentWriter.save(result.toPayment(receiptId));

        if (result.getStatus() == PaymentStatus.DONE) {
            tableWriter.changeTableActiveStatus(false, receipt.getTable());
            receiptWriter.adjustReceipts(java.util.List.of(receipt));
            log.info("토스페이먼츠 결제 승인 완료. receiptId={}, paymentKey={}", receiptId, paymentKey);
        } else {
            log.info("토스페이먼츠 결제 승인 대기. receiptId={}, paymentKey={}, status={}",
                receiptId, paymentKey, result.getStatus());
        }

        return payment;
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
