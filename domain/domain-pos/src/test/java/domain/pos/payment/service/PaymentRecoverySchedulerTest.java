package domain.pos.payment.service;

import static fixtures.payment.PaymentFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.implement.PaymentProcessor;
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;

class PaymentRecoverySchedulerTest extends ServiceTest {

	@Mock
	private PaymentReader paymentReader;
	@Mock
	private PaymentWriter paymentWriter;
	@Mock
	private TossPaymentPort tossPaymentPort;
	@Mock
	private PaymentProcessor paymentProcessor;
	@Mock
	private ReceiptReader receiptReader;

	@InjectMocks
	private PaymentRecoveryScheduler paymentRecoveryScheduler;

	@Nested
	@DisplayName("IN_PROGRESS 결제 복구")
	class RecoverStuckPayments {

		private final Payment inProgressPayment = GENERAL_IN_PROGRESS_PAYMENT();

		@Test
		void 성공_복구_대상_없음() {
			given(paymentReader.findInProgressOlderThan(any(LocalDateTime.class)))
				.willReturn(List.of());

			paymentRecoveryScheduler.recoverStuckPayments();

			verify(tossPaymentPort, never()).getPayment(any());
		}

		@Test
		void 성공_DONE_상태_복구() {
			TossConfirmResult doneResult = TossConfirmResult.builder()
				.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
				.tossOrderId(GENERAL_RECEIPT_ID.toString())
				.amount(GENERAL_AMOUNT)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			given(paymentReader.findInProgressOlderThan(any(LocalDateTime.class)))
				.willReturn(List.of(inProgressPayment));
			given(tossPaymentPort.getPayment(GENERAL_TOSS_PAYMENT_KEY))
				.willReturn(doneResult);
			given(receiptReader.getReceiptWithTableAndStore(GENERAL_RECEIPT_ID))
				.willReturn(Optional.of(receipt));

			paymentRecoveryScheduler.recoverStuckPayments();

			verify(paymentProcessor).finalizeAndSettle(
				eq(GENERAL_PAYMENT_ID), any(TossConfirmResult.class), eq(receipt));
			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_WAITING_FOR_DEPOSIT_상태_복구() {
			TossConfirmResult waitingResult = TossConfirmResult.builder()
				.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
				.tossOrderId(GENERAL_RECEIPT_ID.toString())
				.amount(GENERAL_AMOUNT)
				.status(PaymentStatus.WAITING_FOR_DEPOSIT)
				.paymentMethod("가상계좌")
				.approvedAt(null)
				.build();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			given(paymentReader.findInProgressOlderThan(any(LocalDateTime.class)))
				.willReturn(List.of(inProgressPayment));
			given(tossPaymentPort.getPayment(GENERAL_TOSS_PAYMENT_KEY))
				.willReturn(waitingResult);
			given(receiptReader.getReceiptWithTableAndStore(GENERAL_RECEIPT_ID))
				.willReturn(Optional.of(receipt));

			paymentRecoveryScheduler.recoverStuckPayments();

			verify(paymentProcessor).finalizeAndSettle(
				eq(GENERAL_PAYMENT_ID), any(TossConfirmResult.class), eq(receipt));
			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_미완료_상태_ABORTED_처리() {
			TossConfirmResult abortedResult = TossConfirmResult.builder()
				.tossPaymentKey(GENERAL_TOSS_PAYMENT_KEY)
				.tossOrderId(GENERAL_RECEIPT_ID.toString())
				.amount(GENERAL_AMOUNT)
				.status(PaymentStatus.ABORTED)
				.paymentMethod(null)
				.approvedAt(null)
				.build();

			given(paymentReader.findInProgressOlderThan(any(LocalDateTime.class)))
				.willReturn(List.of(inProgressPayment));
			given(tossPaymentPort.getPayment(GENERAL_TOSS_PAYMENT_KEY))
				.willReturn(abortedResult);

			paymentRecoveryScheduler.recoverStuckPayments();

			verify(paymentWriter).updateStatus(GENERAL_PAYMENT_ID, PaymentStatus.ABORTED);
			verify(paymentProcessor, never()).finalizeAndSettle(any(), any(), any());
		}

		@Test
		void 성공_Toss_미응답_다음_사이클_재시도() {
			given(paymentReader.findInProgressOlderThan(any(LocalDateTime.class)))
				.willReturn(List.of(inProgressPayment));
			given(tossPaymentPort.getPayment(GENERAL_TOSS_PAYMENT_KEY))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

			paymentRecoveryScheduler.recoverStuckPayments();

			// then — 예외를 삼키고 다음 사이클 대기, ABORTED 마킹 없음
			verify(paymentWriter, never()).updateStatus(any(), any());
			verify(paymentProcessor, never()).finalizeAndSettle(any(), any(), any());
		}
	}
}
