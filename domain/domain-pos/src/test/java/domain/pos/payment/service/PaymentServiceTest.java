package domain.pos.payment.service;

import static fixtures.member.UserFixture.*;
import static fixtures.payment.PaymentFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.implement.PaymentProcessor;
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
import domain.pos.payment.implement.PreemptionResult;
import domain.pos.store.entity.Store;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.implement.TableWriter;

class PaymentServiceTest extends ServiceTest {

	@Mock
	private TossPaymentPort tossPaymentPort;
	@Mock
	private PaymentProcessor paymentProcessor;
	@Mock
	private PaymentReader paymentReader;
	@Mock
	private PaymentWriter paymentWriter;
	@Mock
	private ReceiptReader receiptReader;
	@Mock
	private ReceiptWriter receiptWriter;
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private TableWriter tableWriter;

	@InjectMocks
	private PaymentService paymentService;

	@Nested
	@DisplayName("결제 승인")
	class ConfirmPayment {

		private final String paymentKey = GENERAL_TOSS_PAYMENT_KEY;
		private final String orderId = GENERAL_RECEIPT_ID.toString();
		private final Integer amount = GENERAL_AMOUNT;

		@Test
		void 성공() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);
			TossConfirmResult confirmResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(orderId)
				.amount(amount)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();
			Payment savedPayment = GENERAL_DONE_PAYMENT();

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willReturn(confirmResult);
			given(paymentProcessor.finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
				any(TossConfirmResult.class), eq(receipt))).willReturn(savedPayment);

			Payment result = paymentService.confirmPayment(paymentKey, orderId, amount);

			assertSoftly(softly -> {
				softly.assertThat(result.getStatus()).isEqualTo(PaymentStatus.DONE);
				softly.assertThat(result.getTossPaymentKey()).isEqualTo(paymentKey);
				verify(paymentProcessor).validateAndPreempt(any(UUID.class), eq(paymentKey),
					eq(orderId), eq(amount));
				verify(tossPaymentPort).confirm(paymentKey, orderId, amount);
				verify(paymentProcessor).finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
					any(TossConfirmResult.class), eq(receipt));
			});
		}

		@Test
		void 실패_영수증_없음() {
			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willThrow(new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(tossPaymentPort, never()).confirm(any(), any(), any());
				verify(paymentProcessor, never()).finalizeAndSettle(any(), any(), any());
			});
		}

		@Test
		void 실패_이미_정산된_영수증() {
			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willThrow(new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_PAID_RECEIPT);

				verify(tossPaymentPort, never()).confirm(any(), any(), any());
			});
		}

		@Test
		void 실패_결제금액_불일치() {
			Integer wrongAmount = GENERAL_AMOUNT + 1000;
			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(wrongAmount))).willThrow(new ServiceException(ErrorCode.PAYMENT_AMOUNT_MISMATCH));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, wrongAmount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);

				verify(tossPaymentPort, never()).confirm(any(), any(), any());
			});
		}

		@Test
		void 실패_유효하지_않은_orderId() {
			String invalidOrderId = "not-a-uuid";

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, invalidOrderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

				verify(paymentProcessor, never()).validateAndPreempt(any(), any(), any(), any());
				verify(tossPaymentPort, never()).confirm(any(), any(), any());
			});
		}

		@Test
		void 성공_가상계좌_입금대기_영수증_정산_안함() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);
			TossConfirmResult waitingResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(orderId)
				.amount(amount)
				.status(PaymentStatus.WAITING_FOR_DEPOSIT)
				.paymentMethod("가상계좌")
				.approvedAt(null)
				.build();
			Payment savedPayment = GENERAL_WAITING_FOR_DEPOSIT_PAYMENT();

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willReturn(waitingResult);
			given(paymentProcessor.finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
				any(TossConfirmResult.class), eq(receipt))).willReturn(savedPayment);

			paymentService.confirmPayment(paymentKey, orderId, amount);

			verify(paymentProcessor).finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
				any(TossConfirmResult.class), eq(receipt));
		}

		@Test
		void 실패_PG_호출_실패시_선점_레코드_ABORTED_처리() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRM_FAILED);

				verify(paymentWriter).updateStatus(preemptionPayment.getPaymentId(),
					PaymentStatus.ABORTED);
				verify(paymentProcessor, never()).finalizeAndSettle(any(), any(), any());
			});
		}

		@Test
		void 실패_타임아웃_재전송_소진시_선점_레코드_IN_PROGRESS_유지_스케줄러_복구_대기() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_CONFIRM_TIMEOUT));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_IN_PROGRESS);

				// 타임아웃 소진 — ABORTED 마킹 없이 IN_PROGRESS 유지 (스케줄러가 복구)
				verify(paymentWriter, never()).updateStatus(any(), eq(PaymentStatus.ABORTED));
				verify(paymentProcessor, never()).finalizeAndSettle(any(), any(), any());
			});
		}

		@Test
		void 실패_저장_실패시_DONE_결제_보상_취소_및_선점_레코드_ABORTED_처리() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);
			TossConfirmResult doneResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(orderId)
				.amount(amount)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willReturn(doneResult);
			given(paymentProcessor.finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
				any(TossConfirmResult.class), eq(receipt)))
				.willThrow(new RuntimeException("DB 저장 실패"));
			given(tossPaymentPort.cancel(paymentKey, "결제 데이터 저장 실패로 인한 자동 취소", null))
				.willReturn(PaymentStatus.CANCELED);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRM_FAILED);

				verify(tossPaymentPort).cancel(eq(paymentKey),
					eq("결제 데이터 저장 실패로 인한 자동 취소"), isNull());
				verify(paymentWriter).updateStatus(preemptionPayment.getPaymentId(),
					PaymentStatus.ABORTED);
			});
		}

		@Test
		void 실패_저장_실패_보상_취소도_실패해도_PAYMENT_CONFIRM_FAILED_반환() {
			Payment preemptionPayment = GENERAL_IN_PROGRESS_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			PreemptionResult preemptionResult = new PreemptionResult(preemptionPayment, receipt);
			TossConfirmResult doneResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(orderId)
				.amount(amount)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();

			given(paymentProcessor.validateAndPreempt(any(UUID.class), eq(paymentKey), eq(orderId),
				eq(amount))).willReturn(preemptionResult);
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willReturn(doneResult);
			given(paymentProcessor.finalizeAndSettle(eq(preemptionPayment.getPaymentId()),
				any(TossConfirmResult.class), eq(receipt)))
				.willThrow(new RuntimeException("DB 저장 실패"));
			given(tossPaymentPort.cancel(anyString(), anyString(), isNull()))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRM_FAILED);
			});
		}
	}

	@Nested
	@DisplayName("결제 취소")
	class CancelPayment {

		private final String paymentKey = GENERAL_TOSS_PAYMENT_KEY;
		private final String cancelReason = "고객 요청에 의한 취소";

		@Test
		void 성공_전액취소() {
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Payment payment = GENERAL_DONE_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			given(paymentReader.getByTossPaymentKey(paymentKey))
				.willReturn(payment);
			given(receiptReader.getReceiptWithTableAndStore(payment.getReceiptId()))
				.willReturn(Optional.of(receipt));
			given(tossPaymentPort.cancel(paymentKey, cancelReason, null))
				.willReturn(PaymentStatus.CANCELED);
			given(paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED))
				.willReturn(GENERAL_CANCELED_PAYMENT());

			paymentService.cancelPayment(paymentKey, cancelReason, null, ownerPassport);

			verify(tossPaymentPort).cancel(paymentKey, cancelReason, null);
			verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED);
			verify(paymentWriter, never()).save(any());
		}

		@Test
		void 성공_부분취소() {
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Payment payment = GENERAL_DONE_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			Integer cancelAmount = GENERAL_AMOUNT / 2;

			given(paymentReader.getByTossPaymentKey(paymentKey))
				.willReturn(payment);
			given(receiptReader.getReceiptWithTableAndStore(payment.getReceiptId()))
				.willReturn(Optional.of(receipt));
			given(tossPaymentPort.cancel(paymentKey, cancelReason, cancelAmount))
				.willReturn(PaymentStatus.PARTIAL_CANCELED);
			given(paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.PARTIAL_CANCELED))
				.willReturn(Payment.builder()
					.paymentId(GENERAL_PAYMENT_ID).receiptId(GENERAL_RECEIPT_ID)
					.tossPaymentKey(paymentKey).tossOrderId(GENERAL_TOSS_ORDER_ID)
					.amount(GENERAL_AMOUNT).status(PaymentStatus.PARTIAL_CANCELED)
					.paymentMethod(GENERAL_PAYMENT_METHOD).approvedAt(GENERAL_APPROVED_AT).build());

			paymentService.cancelPayment(paymentKey, cancelReason, cancelAmount, ownerPassport);

			verify(tossPaymentPort).cancel(paymentKey, cancelReason, cancelAmount);
			verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.PARTIAL_CANCELED);
		}

		@Test
		void 실패_이미_취소된_결제() {
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Payment canceledPayment = GENERAL_CANCELED_PAYMENT();

			given(paymentReader.getByTossPaymentKey(paymentKey))
				.willReturn(canceledPayment);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.cancelPayment(paymentKey, cancelReason, null, ownerPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCEL_FAILED);

				verify(tossPaymentPort, never()).cancel(any(), any(), any());
				verify(paymentWriter, never()).updateStatus(any(), any());
			});
		}

		@Test
		void 실패_결제_없음() {
			UserPassport ownerPassport = OWNER_USER_PASSPORT();

			given(paymentReader.getByTossPaymentKey(paymentKey))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.cancelPayment(paymentKey, cancelReason, null, ownerPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);

				verify(tossPaymentPort, never()).cancel(any(), any(), any());
				verify(paymentWriter, never()).updateStatus(any(), any());
			});
		}

		@Test
		void 실패_점주_권한_불일치() {
			UserPassport diffOwnerPassport = DIFF_OWNER_PASSPORT();
			Payment payment = GENERAL_DONE_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			given(paymentReader.getByTossPaymentKey(paymentKey))
				.willReturn(payment);
			given(receiptReader.getReceiptWithTableAndStore(payment.getReceiptId()))
				.willReturn(Optional.of(receipt));
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(eq(diffOwnerPassport), any(Store.class));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.cancelPayment(paymentKey, cancelReason, null, diffOwnerPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(tossPaymentPort, never()).cancel(any(), any(), any());
				verify(paymentWriter, never()).updateStatus(any(), any());
			});
		}
	}

	@Nested
	@DisplayName("웹훅 처리")
	class ProcessWebhook {

		private final String paymentKey = GENERAL_TOSS_PAYMENT_KEY;

		@Test
		void 성공_DONE_상태_이미_일치_업데이트_없음() {
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));

			paymentService.processWebhook(paymentKey, "DONE");

			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_CANCELED_상태_업데이트() {
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));
			given(paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED))
				.willReturn(GENERAL_CANCELED_PAYMENT());

			paymentService.processWebhook(paymentKey, "CANCELED");

			verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED);
		}

		@Test
		void 성공_PARTIAL_CANCELED_상태_업데이트() {
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));
			given(paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.PARTIAL_CANCELED))
				.willReturn(GENERAL_PARTIAL_CANCELED_PAYMENT());

			paymentService.processWebhook(paymentKey, "PARTIAL_CANCELED");

			verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.PARTIAL_CANCELED);
		}

		@Test
		void 성공_로컬에_없는_결제키_무시() {
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.empty());

			paymentService.processWebhook(paymentKey, "CANCELED");

			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_알수없는_상태값_무시() {
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));

			paymentService.processWebhook(paymentKey, "ABORTED");

			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_가상계좌_입금완료_DONE_영수증_정산() {
			Payment waitingPayment = GENERAL_WAITING_FOR_DEPOSIT_PAYMENT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(waitingPayment));
			given(paymentWriter.updateStatus(waitingPayment.getPaymentId(), PaymentStatus.DONE))
				.willReturn(GENERAL_DONE_PAYMENT());
			given(receiptReader.getReceiptWithTableAndStore(waitingPayment.getReceiptId()))
				.willReturn(Optional.of(receipt));

			paymentService.processWebhook(paymentKey, "DONE");

			verify(paymentWriter).updateStatus(waitingPayment.getPaymentId(), PaymentStatus.DONE);
			verify(tableWriter).changeTableActiveStatus(eq(false), any());
			verify(receiptWriter).adjustReceipts(anyList());
		}

		@Test
		void 성공_가상계좌_입금완료_이미_정산된_영수증_스킵() {
			Payment waitingPayment = GENERAL_WAITING_FOR_DEPOSIT_PAYMENT();
			Receipt alreadyAdjustedReceipt = GENERAL_ADJUSTMENT_RECEIPT();

			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(waitingPayment));
			given(paymentWriter.updateStatus(waitingPayment.getPaymentId(), PaymentStatus.DONE))
				.willReturn(GENERAL_DONE_PAYMENT());
			given(receiptReader.getReceiptWithTableAndStore(waitingPayment.getReceiptId()))
				.willReturn(Optional.of(alreadyAdjustedReceipt));

			paymentService.processWebhook(paymentKey, "DONE");

			verify(paymentWriter).updateStatus(waitingPayment.getPaymentId(), PaymentStatus.DONE);
			verify(tableWriter, never()).changeTableActiveStatus(anyBoolean(), any());
			verify(receiptWriter, never()).adjustReceipts(anyList());
		}
	}

	@Nested
	@DisplayName("영업별 결제 목록 조회")
	class FindPaymentsBySaleId {

		@Test
		void 성공_결제_있음() {
			Long saleId = 1L;
			List<Payment> payments = List.of(GENERAL_DONE_PAYMENT(), GENERAL_CANCELED_PAYMENT());
			given(paymentReader.findBySaleId(saleId)).willReturn(payments);

			List<Payment> result = paymentService.findPaymentsBySaleId(saleId);

			assertSoftly(softly -> {
				softly.assertThat(result).hasSize(2);
				softly.assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.DONE);
				softly.assertThat(result.get(1).getStatus()).isEqualTo(PaymentStatus.CANCELED);
				verify(paymentReader).findBySaleId(saleId);
			});
		}

		@Test
		void 성공_결제_없음() {
			Long saleId = 999L;
			given(paymentReader.findBySaleId(saleId)).willReturn(List.of());

			List<Payment> result = paymentService.findPaymentsBySaleId(saleId);

			assertSoftly(softly -> softly.assertThat(result).isEmpty());
		}
	}

	@Nested
	@DisplayName("토스 실시간 결제 조회")
	class GetPaymentFromToss {

		private final String paymentKey = GENERAL_TOSS_PAYMENT_KEY;

		@Test
		void 성공() {
			TossConfirmResult tossResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(GENERAL_RECEIPT_ID.toString())
				.amount(GENERAL_AMOUNT)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();

			given(tossPaymentPort.getPayment(paymentKey)).willReturn(tossResult);

			TossConfirmResult result = paymentService.getPaymentFromToss(paymentKey);

			assertSoftly(softly -> {
				softly.assertThat(result.getTossPaymentKey()).isEqualTo(paymentKey);
				softly.assertThat(result.getStatus()).isEqualTo(PaymentStatus.DONE);
				softly.assertThat(result.getAmount()).isEqualTo(GENERAL_AMOUNT);
				verify(tossPaymentPort).getPayment(paymentKey);
			});
		}

		@Test
		void 실패_토스에서_결제_없음() {
			given(tossPaymentPort.getPayment(paymentKey))
				.willThrow(new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

			// when -> then
			assertSoftly(softly -> softly.assertThatThrownBy(
					() -> paymentService.getPaymentFromToss(paymentKey))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND));
		}
	}
}
