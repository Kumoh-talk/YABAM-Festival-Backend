package domain.pos.call.service;

import static fixtures.call.CallFixture.*;
import static fixtures.member.UserFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.table.TableFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
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
import domain.pos.call.entity.CallMessage;
import domain.pos.call.implement.CallReader;
import domain.pos.call.implement.CallWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import fixtures.store.SaleFixture;

class CallServiceTest extends ServiceTest {

	@Mock
	private ReceiptReader receiptReader;

	@Mock
	private CallWriter callWriter;

	@Mock
	private CallReader callReader;

	@InjectMocks
	private CallService callService;

	@Nested
	@DisplayName("직원 호출 요청(postCall) 시")
	class PostCall {

		@Test
		void 성공() {
			// given
			Store store = GENERAL_OPEN_STORE();
			Table table = GENERAL_ACTIVE_TABLE(store);
			Sale sale = SaleFixture.GENERAL_OPEN_SALE(store);
			Receipt receipt = CUSTOM_ADJUSTMENT_RECEIPT(sale, table); // isActive = true, store.isOpen = true
			CallMessage queryCallMessage = CUSTOM_GENERAL_CALL(sale, table, receipt).getCallMessage();
			Long queryStoreId = store.getStoreId();
			UUID receiptId = receipt.getReceiptInfo().getReceiptId();

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(any(UUID.class));

			// when
			callService.postCall(receiptId, queryStoreId, queryCallMessage);

			// then
			assertSoftly(softly -> {
				verify(receiptReader)
					.getReceiptWithTableAndStore(any(UUID.class));
				verify(callWriter)
					.createCall(any(UUID.class), anyLong(), any(CallMessage.class));
			});
		}

		@Nested
		@DisplayName("직원 호출 요청(postCall) 실패")
		class PostCallFail {

			private final CallMessage queryCallMessage = GENERAL_CALL_MESSAGE();

			@Test
			@DisplayName("영수증이 존재하지 않으면 RECEIPT_NOT_FOUND")
			void 실패_영수증_없음() {
				// given
				UUID queryReceiptId = UUID.randomUUID();
				Long queryStoreId = 10L;

				doReturn(Optional.empty())
					.when(receiptReader).getReceiptWithTableAndStore(any(UUID.class));

				// when -> then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(
							() -> callService.postCall(queryReceiptId, queryStoreId, queryCallMessage))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

					verify(receiptReader).getReceiptWithTableAndStore(any(UUID.class));
					verify(callWriter, never()).createCall(any(UUID.class), anyLong(), any(CallMessage.class));
				});
			}

			@Test
			@DisplayName("영수증이 닫힌 가게와 연결돼 있으면 CONFLICT_CLOSE_STORE")
			void 실패_가게_종료() {
				// given
				Store store = GENERAL_CLOSE_STORE();           // isOpen = false
				Table table = GENERAL_ACTIVE_TABLE(store);
				Sale sale = SaleFixture.GENERAL_OPEN_SALE(store);
				Receipt receipt = CUSTOM_ADJUSTMENT_RECEIPT(sale, table);

				UUID queryReceiptId = receipt.getReceiptInfo().getReceiptId();
				Long queryStoreId = store.getStoreId();

				doReturn(Optional.of(receipt))
					.when(receiptReader).getReceiptWithTableAndStore(any(UUID.class));

				// when -> then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(
							() -> callService.postCall(queryReceiptId, queryStoreId, queryCallMessage))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT_CLOSE_STORE);

					verify(callWriter, never()).createCall(any(UUID.class), anyLong(), any(CallMessage.class));
				});
			}

			@Test
			@DisplayName("비활성 테이블이면 TABLE_NOT_ACTIVE")
			void 실패_테이블_비활성() {
				// given
				Store store = GENERAL_OPEN_STORE();
				Table table = GENERAL_IN_ACTIVE_TABLE(store);  // isActive = false
				Sale sale = SaleFixture.GENERAL_OPEN_SALE(store);
				Receipt receipt = CUSTOM_ADJUSTMENT_RECEIPT(sale, table);

				UUID queryReceiptId = receipt.getReceiptInfo().getReceiptId();
				Long queryStoreId = store.getStoreId();

				doReturn(Optional.of(receipt))
					.when(receiptReader).getReceiptWithTableAndStore(any(UUID.class));

				// when -> then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(
							() -> callService.postCall(queryReceiptId, queryStoreId, queryCallMessage))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.TABLE_NOT_ACTIVE);

					verify(callWriter, never()).createCall(any(UUID.class), anyLong(), any(CallMessage.class));
				});
			}

			@Test
			@DisplayName("스토어 ID 불일치 시 STORE_NOT_MATCH")
			void 실패_스토어_ID_불일치() {
				// given
				Store store = GENERAL_OPEN_STORE();            // storeId = 10
				Table table = GENERAL_ACTIVE_TABLE(store);
				Sale sale = SaleFixture.GENERAL_OPEN_SALE(store);
				Receipt receipt = CUSTOM_ADJUSTMENT_RECEIPT(sale, table);

				UUID queryReceiptId = receipt.getReceiptInfo().getReceiptId();
				Long wrongStoreId = 999L;

				doReturn(Optional.of(receipt))
					.when(receiptReader).getReceiptWithTableAndStore(any(UUID.class));

				// when -> then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(
							() -> callService.postCall(queryReceiptId, wrongStoreId, queryCallMessage))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_MATCH);

					verify(callWriter, never()).createCall(any(UUID.class), anyLong(), any(CallMessage.class));
				});
			}
		}
	}

	@Nested
	@DisplayName("직원 호출 완료(completeCall)")
	class CompleteCall {

		private final Long callId = 1L;
		private final UserPassport ownerPass = OWNER_USER_PASSPORT();

		@Test
		@DisplayName("성공")
		void 성공() {
			// when
			callService.completeCall(ownerPass, callId);

			// then
			assertSoftly(softly -> {
				verify(callReader).validateCallOwner(callId, ownerPass);
				verify(callWriter).completeCall(callId);
			});
		}

		@Test
		@DisplayName("호출 소유자가 아니면 NOT_VALID_CALL_OWNER")
		void 실패_유효하지_않은_OWNER() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_VALID_CALL_OWNER))
				.when(callReader).validateCallOwner(callId, ownerPass);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> callService.completeCall(ownerPass, callId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_VALID_CALL_OWNER);

				verify(callReader).validateCallOwner(callId, ownerPass);
				verify(callWriter, never()).completeCall(anyLong());
			});
		}
	}
}
