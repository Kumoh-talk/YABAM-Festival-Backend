package domain.pos.payment.service;

import static fixtures.member.UserFixture.*;
import static fixtures.payment.PaymentFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

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
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.store.entity.Store;
import domain.pos.payment.implement.PaymentReader;
import domain.pos.payment.implement.PaymentWriter;
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
			// given
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			TossConfirmResult confirmResult = TossConfirmResult.builder()
				.tossPaymentKey(paymentKey)
				.tossOrderId(orderId)
				.amount(amount)
				.status(PaymentStatus.DONE)
				.paymentMethod(GENERAL_PAYMENT_METHOD)
				.approvedAt(GENERAL_APPROVED_AT)
				.build();
			Payment savedPayment = GENERAL_DONE_PAYMENT();

			given(receiptReader.getReceiptWithTableAndStore(any(UUID.class)))
				.willReturn(Optional.of(receipt));
			given(paymentReader.findByReceiptId(any(UUID.class)))
				.willReturn(Optional.empty());
			given(tossPaymentPort.confirm(paymentKey, orderId, amount))
				.willReturn(confirmResult);
			given(paymentWriter.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			Payment result = paymentService.confirmPayment(paymentKey, orderId, amount);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result.getStatus()).isEqualTo(PaymentStatus.DONE);
				softly.assertThat(result.getTossPaymentKey()).isEqualTo(paymentKey);
				verify(tossPaymentPort).confirm(paymentKey, orderId, amount);
				verify(paymentWriter).save(any(Payment.class));
				verify(tableWriter).changeTableActiveStatus(eq(false), any());
				verify(receiptWriter).adjustReceipts(anyList());
			});
		}

		@Test
		void 실패_영수증_없음() {
			// given
			given(receiptReader.getReceiptWithTableAndStore(any(UUID.class)))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(tossPaymentPort, never()).confirm(any(), any(), any());
				verify(paymentWriter, never()).save(any());
			});
		}

		@Test
		void 실패_이미_정산된_영수증() {
			// given
			Receipt adjustedReceipt = GENERAL_ADJUSTMENT_RECEIPT();

			given(receiptReader.getReceiptWithTableAndStore(any(UUID.class)))
				.willReturn(Optional.of(adjustedReceipt));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, orderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_PAID_RECEIPT);

				verify(paymentReader, never()).findByReceiptId(any());
				verify(tossPaymentPort, never()).confirm(any(), any(), any());
			});
		}

		@Test
		void 실패_이미_결제된_영수증() {
			// given
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			Payment existingPayment = GENERAL_DONE_PAYMENT();

			given(receiptReader.getReceiptWithTableAndStore(any(UUID.class)))
				.willReturn(Optional.of(receipt));
			given(paymentReader.findByReceiptId(any(UUID.class)))
				.willReturn(Optional.of(existingPayment));

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
			// given
			ReceiptInfo feeReceiptInfo = ReceiptInfo.builder()
				.receiptId(GENERAL_RECEIPT_ID)
				.isAdjustment(false)
				.occupancyFee(GENERAL_AMOUNT)
				.build();
			Receipt receipt = CUSTOM_RECEIPT(feeReceiptInfo, GENERAL_OPEN_SALE, GENERAL_TABLE);
			Integer wrongAmount = GENERAL_AMOUNT + 1000;

			given(receiptReader.getReceiptWithTableAndStore(any(UUID.class)))
				.willReturn(Optional.of(receipt));
			given(paymentReader.findByReceiptId(any(UUID.class)))
				.willReturn(Optional.empty());

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
			// given
			String invalidOrderId = "not-a-uuid";

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentService.confirmPayment(paymentKey, invalidOrderId, amount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

				verify(receiptReader, never()).getReceiptWithTableAndStore(any());
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
			// given
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

			// when
			paymentService.cancelPayment(paymentKey, cancelReason, null, ownerPassport);

			// then
			assertSoftly(softly -> {
				verify(tossPaymentPort).cancel(paymentKey, cancelReason, null);
				verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED);
				verify(paymentWriter, never()).save(any());
			});
		}

		@Test
		void 성공_부분취소() {
			// given
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

			// when
			paymentService.cancelPayment(paymentKey, cancelReason, cancelAmount, ownerPassport);

			// then
			assertSoftly(softly -> {
				verify(tossPaymentPort).cancel(paymentKey, cancelReason, cancelAmount);
				verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.PARTIAL_CANCELED);
			});
		}

		@Test
		void 실패_이미_취소된_결제() {
			// given
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
			// given
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
			// given
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
			// given
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));

			// when
			paymentService.processWebhook(paymentKey, "DONE");

			// then
			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_CANCELED_상태_업데이트() {
			// given
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));
			given(paymentWriter.updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED))
				.willReturn(GENERAL_CANCELED_PAYMENT());

			// when
			paymentService.processWebhook(paymentKey, "CANCELED");

			// then
			verify(paymentWriter).updateStatus(payment.getPaymentId(), PaymentStatus.CANCELED);
		}

		@Test
		void 성공_로컬에_없는_결제키_무시() {
			// given
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.empty());

			// when
			paymentService.processWebhook(paymentKey, "CANCELED");

			// then
			verify(paymentWriter, never()).updateStatus(any(), any());
		}

		@Test
		void 성공_알수없는_상태값_무시() {
			// given
			Payment payment = GENERAL_DONE_PAYMENT();
			given(paymentReader.findByTossPaymentKey(paymentKey))
				.willReturn(Optional.of(payment));

			// when
			paymentService.processWebhook(paymentKey, "ABORTED");

			// then
			verify(paymentWriter, never()).updateStatus(any(), any());
		}
	}
}
