package domain.pos.receipt.service;

import static fixtures.member.UserFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static fixtures.store.SaleFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.table.TableFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.SaleReader;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;
import fixtures.member.UserFixture;

class ReceiptServiceTest extends ServiceTest {

	@Mock
	private ReceiptValidator receiptValidator;
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private SaleReader saleReader;
	@Mock
	private TableWriter tableWriter;
	@Mock
	private TableReader tableReader;
	@Mock
	private ReceiptWriter receiptWriter;
	@Mock
	private ReceiptReader receiptReader;
	@Mock
	private domain.pos.member.implement.UserPassportValidator userPassportValidator;

	@InjectMocks
	private ReceiptService receiptService;

	@Nested
	@DisplayName("테이블 이동(moveReceiptTable)")
	class MoveReceiptTable {

		private final UserPassport ownerPassport = OWNER_USER_PASSPORT();
		private final UUID receiptId = UUID.randomUUID();
		private final UUID moveTableId = UUID.randomUUID();

		@Test
		@DisplayName("성공")
		void 성공() {
			Store store = GENERAL_OPEN_STORE();
			Sale sale = GENERAL_OPEN_SALE(store);
			Table currentTable = GENERAL_ACTIVE_TABLE(store);
			Table moveTable = GENERAL_IN_ACTIVE_TABLE(store);
			Receipt receipt = CUSTOM_RECEIPT(GENERAL_NON_ADJUSTMENT_RECEIPT_INFO, sale, currentTable);

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);
			doReturn(Optional.of(moveTable))
				.when(tableReader).findLockTableById(moveTableId, store.getId());

			receiptService.moveReceiptTable(ownerPassport, receiptId, moveTableId);

			assertSoftly(softly -> {
				verify(storeValidator).validateStoreOwner(ownerPassport, store);
				verify(receiptWriter).moveReceiptTable(receipt, moveTable);
				verify(tableWriter).changeTableActiveStatus(true, moveTable);
				verify(tableWriter).changeTableActiveStatus(false, currentTable);
			});
		}

		@Test
		@DisplayName("영수증이 존재하지 않으면 RECEIPT_NOT_FOUND")
		void 실패_영수증_없음() {
			doReturn(Optional.empty())
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.moveReceiptTable(ownerPassport, receiptId, moveTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(storeValidator, never()).validateStoreOwner(any(), any(Store.class));
				verify(receiptWriter, never()).moveReceiptTable(any(), any());
			});
		}

		@Test
		@DisplayName("가게 점주가 아니면 NOT_EQUAL_STORE_OWNER")
		void 실패_점주_불일치() {
			Store store = GENERAL_OPEN_STORE();
			Sale sale = GENERAL_OPEN_SALE(store);
			Table currentTable = GENERAL_ACTIVE_TABLE(store);
			Receipt receipt = CUSTOM_RECEIPT(GENERAL_NON_ADJUSTMENT_RECEIPT_INFO, sale, currentTable);

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), any(Store.class));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.moveReceiptTable(ownerPassport, receiptId, moveTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(receiptWriter, never()).moveReceiptTable(any(), any());
			});
		}

		@Test
		@DisplayName("이동할 테이블이 이미 활성화돼 있으면 ALREADY_ACTIVE_TABLE")
		void 실패_이동_테이블_이미_활성() {
			Store store = GENERAL_OPEN_STORE();
			Sale sale = GENERAL_OPEN_SALE(store);
			Table currentTable = GENERAL_ACTIVE_TABLE(store);
			Table alreadyActiveTable = GENERAL_ACTIVE_TABLE(store);
			Receipt receipt = CUSTOM_RECEIPT(GENERAL_NON_ADJUSTMENT_RECEIPT_INFO, sale, currentTable);

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);
			doReturn(Optional.of(alreadyActiveTable))
				.when(tableReader).findLockTableById(moveTableId, store.getId());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.moveReceiptTable(ownerPassport, receiptId, moveTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ACTIVE_TABLE);

				verify(storeValidator).validateStoreOwner(ownerPassport, store);
				verify(receiptWriter, never()).moveReceiptTable(any(), any());
			});
		}

		@Test
		@DisplayName("이동할 테이블을 찾을 수 없으면 NOT_FOUND_TABLE")
		void 실패_이동_테이블_없음() {
			Store store = GENERAL_OPEN_STORE();
			Sale sale = GENERAL_OPEN_SALE(store);
			Table currentTable = GENERAL_ACTIVE_TABLE(store);
			Receipt receipt = CUSTOM_RECEIPT(GENERAL_NON_ADJUSTMENT_RECEIPT_INFO, sale, currentTable);

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);
			doReturn(Optional.empty())
				.when(tableReader).findLockTableById(moveTableId, store.getId());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.moveReceiptTable(ownerPassport, receiptId, moveTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TABLE);

				verify(storeValidator).validateStoreOwner(ownerPassport, store);
				verify(receiptWriter, never()).moveReceiptTable(any(), any());
			});
		}
	}

	@Nested
	@DisplayName("영수증 정산(adjustReceipts)")
	class AdjustReceipts {

		private final List<UUID> receiptIds = List.of(UUID.randomUUID());
		private final UserPassport ownerPassport = OWNER_USER_PASSPORT();

		@Test
		@DisplayName("성공")
		void 성공() {
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			List<Receipt> receipts = List.of(receipt);

			doReturn(receipts)
				.when(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);

			receiptService.adjustReceipts(receiptIds, ownerPassport);

			assertSoftly(softly -> {
				verify(receiptValidator).validateIsOwner(receipt, ownerPassport);
				verify(tableWriter).changeTableActiveStatus(false, receipt.getTable());
				verify(receiptWriter).adjustReceipts(receipts);
			});
		}

		@Test
		@DisplayName("영수증 조회 결과가 요청 개수와 다르면 RECEIPT_NOT_FOUND")
		void 실패_영수증_없음() {
			doReturn(List.of())
				.when(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.adjustReceipts(receiptIds, ownerPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptWriter, never()).adjustReceipts(anyList());
			});
		}
	}

	@Nested
	@DisplayName("영수증 삭제(deleteReceipt)")
	class DeleteReceipt {

		private final UUID receiptId = UUID.randomUUID();
		private final UserPassport ownerPassport = UserFixture.OWNER_USER_PASSPORT();

		@Test
		@DisplayName("미정산 영수증 삭제 시 테이블 비활성화")
		void 성공_미정산_영수증() {
			Store store = GENERAL_OPEN_STORE();
			Sale sale = GENERAL_OPEN_SALE(store);
			Table table = GENERAL_ACTIVE_TABLE(store);
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			doReturn(Optional.of(receipt))
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);

			receiptService.deleteReceipt(receiptId, ownerPassport);

			assertSoftly(softly -> {
				verify(receiptValidator).validateIsOwner(receipt, ownerPassport);
				verify(tableWriter).changeTableActiveStatus(false, receipt.getTable());
				verify(receiptWriter).deleteReceipt(receiptId);
			});
		}

		@Test
		@DisplayName("영수증이 존재하지 않으면 RECEIPT_NOT_FOUND")
		void 실패_영수증_없음() {
			doReturn(Optional.empty())
				.when(receiptReader).getReceiptWithTableAndStore(receiptId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.deleteReceipt(receiptId, ownerPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptValidator, never()).validateIsOwner(any(), any());
				verify(receiptWriter, never()).deleteReceipt(any());
			});
		}
	}
}
