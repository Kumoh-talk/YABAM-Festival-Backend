package domain.pos.receipt.service;

import static fixtures.member.UserFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static fixtures.receipt.ReceiptInfoFixture.*;
import static fixtures.store.SaleFixture.*;
import static fixtures.table.TableFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.SaleReader;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;
import fixtures.member.UserFixture;
import fixtures.receipt.ReceiptFixture;
import fixtures.store.StoreFixture;

class ReceiptServiceTest extends ServiceTest {

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

	@InjectMocks
	private ReceiptService receiptService;

	@Nested
	@DisplayName("영수증 생성")
	class registerReceipt {
		@Test
		void 성공() {
			// given
			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
			Table savedInActiveTable = GENERAL_IN_ACTIVE_TABLE(savedStore);
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			ReceiptInfo receiptInfo = NON_ADJUSTMENT_RECEIPT_INFO();
			UserPassport queryUserPassport = UserFixture.GENERAL_USER_PASSPORT();
			Long queryTableId = savedInActiveTable.getTableId();
			Long querySaleId = savedSale.getSaleId();

			Table changedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
			Receipt createdReceipt = CUSTOM_RECEIPT(
				receiptInfo,
				queryUserPassport,
				changedActiveTable,
				savedSale
			);
			doReturn(Optional.of(savedInActiveTable))
				.when(tableReader).findLockTableById(queryTableId);
			doReturn(Optional.of(savedSale))
				.when(saleReader).readSingleSale(querySaleId);
			doReturn(changedActiveTable)
				.when(tableWriter).changeTableActiveStatus(true, savedInActiveTable);
			doReturn(createdReceipt)
				.when(receiptWriter).createReceipt(queryUserPassport, changedActiveTable, savedSale);
			// when
			Receipt savedReceipt = receiptService.registerReceipt(
				queryUserPassport,
				queryTableId,
				querySaleId
			);
			// then
			assertSoftly(softly -> {
				softly.assertThat(savedReceipt.getTable()).isEqualTo(changedActiveTable);
				softly.assertThat(savedReceipt.getSale()).isEqualTo(savedSale);
				softly.assertThat(savedReceipt.getReceiptInfo().isAdjustment()).isFalse();
				softly.assertThat(savedReceipt.getTable().getIsActive()).isTrue();

				verify(saleReader).readSingleSale(anyLong());
				verify(tableReader).findLockTableById(anyLong());
				verify(tableWriter).changeTableActiveStatus(anyBoolean(), any(Table.class));
				verify(receiptWriter).createReceipt(any(UserPassport.class), any(Table.class), any(Sale.class));
			});

		}

		@Test
		void 실패_테이블이_활성화되어있는데_요청을_보낸_경우() {
			// given
			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			UserPassport queryUserPassport = UserFixture.GENERAL_USER_PASSPORT();
			Long queryTableId = savedActiveTable.getTableId();
			Long querySaleId = savedSale.getSaleId();

			doReturn(Optional.of(savedActiveTable))
				.when(tableReader).findLockTableById(queryTableId);
			doReturn(Optional.of(savedSale))
				.when(saleReader).readSingleSale(querySaleId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.registerReceipt(queryUserPassport, queryTableId, querySaleId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ACTIVE_TABLE);

				verify(saleReader).readSingleSale(anyLong());
				verify(tableReader).findLockTableById(anyLong());
				verify(tableWriter, never())
					.changeTableActiveStatus(anyBoolean(), any(Table.class));
				verify(receiptWriter, never())
					.createReceipt(any(UserPassport.class), any(Table.class), any(Sale.class));
			});
		}

		@Test
		void 실패_유효하지_않은_sale_id() {
			// given
			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			UserPassport queryUserPassport = UserFixture.GENERAL_USER_PASSPORT();
			Long queryTableId = savedActiveTable.getTableId();
			Long querySaleId = savedSale.getSaleId();

			doReturn(Optional.empty())
				.when(saleReader).readSingleSale(querySaleId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.registerReceipt(queryUserPassport, queryTableId, querySaleId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SALE);

				verify(saleReader).readSingleSale(anyLong());
				verify(tableReader, never())
					.findLockTableById(anyLong());
				verify(tableWriter, never())
					.changeTableActiveStatus(anyBoolean(), any(Table.class));
				verify(receiptWriter, never())
					.createReceipt(any(UserPassport.class), any(Table.class), any(Sale.class));
			});
		}

		@Test
		void 실패_유효하지_않은_table_id() {
			// given
			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			UserPassport queryUserPassport = UserFixture.GENERAL_USER_PASSPORT();
			Long queryTableId = savedActiveTable.getTableId();
			Long querySaleId = savedSale.getSaleId();

			doReturn(Optional.of(savedSale))
				.when(saleReader).readSingleSale(querySaleId);
			doReturn(Optional.empty())
				.when(tableReader).findLockTableById(queryTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.registerReceipt(queryUserPassport, queryTableId, querySaleId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TABLE);

				verify(saleReader).readSingleSale(anyLong());
				verify(tableReader).findLockTableById(anyLong());
				verify(tableWriter, never())
					.changeTableActiveStatus(anyBoolean(), any(Table.class));
				verify(receiptWriter, never())
					.createReceipt(any(UserPassport.class), any(Table.class), any(Sale.class));
			});
		}
	}

	@Nested
	@DisplayName("영수증 상세 조회")
	class getReceiptInfo {
		private final Long receiptId = 1L;
		private UserPassport userPassport = CUSTOM_USER_PASSPORT(
			2L,
			"요청 유저",
			UserRole.ROLE_USER
		);

		@Test
		void 영수증_상세_조회_성공() {
			// given
			Receipt receipt = CUSTOM_RECEIPT(
				NON_ADJUSTMENT_RECEIPT_INFO(),
				userPassport,
				GENERAL_ACTIVE_TABLE(StoreFixture.GENERAL_OPEN_STORE()),
				GENERAL_OPEN_SALE(StoreFixture.GENERAL_OPEN_STORE())
			);

			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when
			ReceiptInfo receiptInfo = receiptService.getReceiptInfo(receiptId, userPassport);

			// then
			assertSoftly(softly -> {
				softly.assertThat(receiptInfo).isEqualTo(receipt.getReceiptInfo());
			});

		}

		@Test
		void 영수증_조회_실패() {
			// given
			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.getReceiptInfo(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			UserPassport anotherUserPassport = UserFixture.CUSTOM_USER_PASSPORT(
				100L,
				"다른유저",
				UserRole.ROLE_USER
			);

			Receipt receipt = CUSTOM_RECEIPT(
				NON_ADJUSTMENT_RECEIPT_INFO(),
				userPassport,
				GENERAL_ACTIVE_TABLE(StoreFixture.GENERAL_OPEN_STORE()),
				GENERAL_OPEN_SALE(StoreFixture.GENERAL_OPEN_STORE())
			);

			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.getReceiptInfo(receiptId, anotherUserPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
			});
		}

	}

	@Nested
	@DisplayName("영수증 목록 조회")
	class getReceiptPageBySale {
		private final int size = 10;
		private final Pageable pageable = Pageable.ofSize(size);
		private UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();
		private final Long saleId = 1L;

		@Test
		void 영수증_목록_조회_성공() {
			// given
			Store savedStore = StoreFixture.GENERAL_OPEN_STORE();
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			BDDMockito.given(saleReader.readSaleWithOwner(saleId))
				.willReturn(Optional.of(savedSale));

			// when
			receiptService.getReceiptPageBySale(pageable, userPassport, saleId);

			// then
			verify(saleReader).readSaleWithOwner(saleId);
			verify(receiptReader).getReceiptPageBySale(pageable, saleId);
		}

		@Test
		void 실패_유효하지_않은_sale_id() {
			// given
			Sale savedSale = GENERAL_OPEN_SALE(StoreFixture.GENERAL_OPEN_STORE());

			BDDMockito.given(saleReader.readSaleWithOwner(saleId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.getReceiptPageBySale(pageable, userPassport, savedSale.getSaleId()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SALE);

				verify(saleReader).readSaleWithOwner(saleId);
				verify(receiptReader, never()).getReceiptPageBySale(pageable, saleId);
			});
		}

		@Test
		void 실패_요청_유저_점주_불일치() {
			// given
			userPassport = DIFF_OWNER_PASSPORT();
			Store savedStore = StoreFixture.GENERAL_OPEN_STORE();
			Sale savedSale = GENERAL_OPEN_SALE(savedStore);

			BDDMockito.given(saleReader.readSaleWithOwner(saleId))
				.willReturn(Optional.of(savedSale));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.getReceiptPageBySale(pageable, DIFF_OWNER_PASSPORT(), saleId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_VALID_OWNER);

				verify(saleReader).readSaleWithOwner(saleId);
				verify(receiptReader, never()).getReceiptPageBySale(pageable, saleId);
			});
		}
	}

	@Nested
	@DisplayName("영수증 정산")
	class adjustReceipt {
		private final Long receiptId = 1L;
		private UserPassport userPassport = ReceiptFixture.GENERAL_USER_PASSPORT;

		@Test
		void 영수증_정산_성공() {
			// given
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when
			receiptService.adjustReceipt(receiptId, userPassport);

			// then
			verify(receiptReader).getReceiptWithOwner(receiptId);
			verify(receiptWriter).adjustReceipt(receiptId);
		}

		@Test
		void 실패_요청_유저_점주_불일치() {
			// given
			userPassport = DIFF_OWNER_PASSPORT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.adjustReceipt(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_VALID_OWNER);

				verify(receiptReader).getReceiptWithOwner(receiptId);
				verify(receiptWriter, never()).adjustReceipt(receiptId);
			});
		}

		@Test
		void 실패_정산된_영수증() {
			// given
			Receipt receipt = GENERAL_ADJUSTMENT_RECEIPT();

			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.adjustReceipt(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);

				verify(receiptReader).getReceiptWithOwner(receiptId);
				verify(receiptWriter, never()).adjustReceipt(receiptId);
			});
		}
	}

	@Nested
	@DisplayName("영수증 삭제")
	class deleteReceipt {
		private final Long receiptId = 1L;
		private UserPassport userPassport = ReceiptFixture.GENERAL_USER_PASSPORT;

		@Test
		void 영수증_삭제_성공() {
			// given
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when
			receiptService.deleteReceipt(receiptId, userPassport);

			// then
			verify(receiptReader).getReceiptWithOwner(receiptId);
			verify(receiptWriter).deleteReceipt(receiptId);
		}

		@Test
		void 영수증_조회_실패() {
			// given
			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.deleteReceipt(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptReader).getReceiptWithOwner(receiptId);
				verify(receiptWriter, never()).deleteReceipt(receiptId);
			});
		}

		@Test
		void 실패_요청_유저_점주_불일치() {
			// given
			userPassport = DIFF_OWNER_PASSPORT();
			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();

			BDDMockito.given(receiptReader.getReceiptWithOwner(receiptId))
				.willReturn(Optional.of(receipt));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> receiptService.deleteReceipt(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_VALID_OWNER);

				verify(receiptReader).getReceiptWithOwner(receiptId);
				verify(receiptWriter, never()).deleteReceipt(receiptId);
			});
		}
	}
}
