// package domain.pos.receipt.service;
//
// import static fixtures.member.UserFixture.*;
// import static fixtures.receipt.ReceiptFixture.*;
// import static fixtures.receipt.ReceiptInfoFixture.*;
// import static fixtures.store.SaleFixture.GENERAL_OPEN_SALE;
// import static fixtures.table.TableFixture.*;
// import static org.assertj.core.api.SoftAssertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.any;
// import static org.mockito.Mockito.anyBoolean;
// import static org.mockito.Mockito.anyLong;
// import static org.mockito.Mockito.*;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.mockito.BDDMockito;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.springframework.data.domain.Pageable;
//
// import com.exception.ErrorCode;
// import com.exception.ServiceException;
// import com.vo.UserPassport;
//
// import base.ServiceTest;
// import domain.pos.receipt.entity.Receipt;
// import domain.pos.receipt.entity.ReceiptInfo;
// import domain.pos.receipt.implement.ReceiptReader;
// import domain.pos.receipt.implement.ReceiptValidator;
// import domain.pos.receipt.implement.ReceiptWriter;
// import domain.pos.sale.entity.Sale;
// import domain.pos.store.entity.Store;
// import domain.pos.store.implement.SaleReader;
// import domain.pos.store.implement.StoreValidator;
// import domain.pos.table.entity.Table;
// import domain.pos.table.implement.TableReader;
// import domain.pos.table.implement.TableWriter;
// import fixtures.member.UserFixture;
// import fixtures.receipt.ReceiptInfoFixture;
// import fixtures.store.StoreFixture;
//
// class ReceiptServiceTest extends ServiceTest {
//
// 	@Mock
// 	private ReceiptValidator receiptValidator;
// 	@Mock
// 	private StoreValidator storeValidator;
//
// 	@Mock
// 	private SaleReader saleReader;
// 	@Mock
// 	private TableWriter tableWriter;
// 	@Mock
// 	private TableReader tableReader;
//
// 	@Mock
// 	private ReceiptWriter receiptWriter;
// 	@Mock
// 	private ReceiptReader receiptReader;
//
// 	@InjectMocks
// 	private ReceiptService receiptService;
//
// 	@Nested
// 	@DisplayName("영수증 생성")
// 	class registerReceipt {
// 		@Test
// 		void 성공() {
// 			// given
// 			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
// 			Table savedInActiveTable = GENERAL_IN_ACTIVE_TABLE(savedStore);
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			ReceiptInfo receiptInfo = NON_ADJUSTMENT_RECEIPT_INFO();
// 			Long queryTableId = savedInActiveTable.getTableId();
// 			Long querySaleId = savedSale.getSaleId();
//
// 			Table changedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
// 			Receipt createdReceipt = CUSTOM_RECEIPT(
// 				receiptInfo,
// 				savedSale,
// 				changedActiveTable
// 			);
//
// 			BDDMockito.given(saleReader.readSingleSale(querySaleId))
// 				.willReturn(Optional.of(savedSale));
// 			BDDMockito.given(tableReader.findLockTableById(queryTableId, savedSale.getStore().getStoreId()))
// 				.willReturn(Optional.of(savedInActiveTable));
// 			BDDMockito.given(tableWriter.changeTableActiveStatus(true, savedInActiveTable))
// 				.willReturn(changedActiveTable);
// 			BDDMockito.given(receiptWriter.createReceipt(changedActiveTable, savedSale))
// 				.willReturn(createdReceipt);
//
// 			// when
// 			Receipt savedReceipt = receiptService.registerReceipt(
// 				queryTableId,
// 				querySaleId
// 			);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(savedReceipt.getTable()).isEqualTo(changedActiveTable);
// 				softly.assertThat(savedReceipt.getSale()).isEqualTo(savedSale);
// 				softly.assertThat(savedReceipt.getReceiptInfo().isAdjustment()).isFalse();
// 				softly.assertThat(savedReceipt.getTable().getIsActive()).isTrue();
//
// 				verify(saleReader).readSingleSale(querySaleId);
// 				verify(tableReader).findLockTableById(queryTableId, savedSale.getStore().getStoreId());
// 				verify(tableWriter).changeTableActiveStatus(true, savedInActiveTable);
// 				verify(receiptWriter).createReceipt(changedActiveTable, savedSale);
// 			});
//
// 		}
//
// 		@Test
// 		void 실패_테이블이_활성화되어있는데_요청을_보낸_경우() {
// 			// given
// 			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
// 			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			Long queryTableId = savedActiveTable.getTableId();
// 			Long querySaleId = savedSale.getSaleId();
//
// 			BDDMockito.given(saleReader.readSingleSale(querySaleId))
// 				.willReturn(Optional.of(savedSale));
// 			BDDMockito.given(tableReader.findLockTableById(queryTableId, savedSale.getStore().getStoreId()))
// 				.willReturn(Optional.of(savedActiveTable));
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.registerReceipt(queryTableId, querySaleId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ACTIVE_TABLE);
//
// 				verify(saleReader).readSingleSale(querySaleId);
// 				verify(tableReader).findLockTableById(queryTableId, savedSale.getStore().getStoreId());
// 				verify(tableWriter, never()).changeTableActiveStatus(anyBoolean(), any());
// 				verify(receiptWriter, never()).createReceipt(any(), any());
// 			});
// 		}
//
// 		@Test
// 		void 실패_유효하지_않은_sale_id() {
// 			// given
// 			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
// 			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			Long queryTableId = savedActiveTable.getTableId();
// 			Long querySaleId = savedSale.getSaleId();
//
// 			BDDMockito.given(saleReader.readSingleSale(querySaleId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.registerReceipt(queryTableId, querySaleId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SALE);
//
// 				verify(saleReader).readSingleSale(querySaleId);
// 				verify(tableReader, never()).findLockTableById(anyLong(), anyLong());
// 				verify(tableWriter, never()).changeTableActiveStatus(anyBoolean(), any());
// 				verify(receiptWriter, never()).createReceipt(any(), any());
// 			});
// 		}
//
// 		@Test
// 		void 실패_유효하지_않은_table_id() {
// 			// given
// 			Store savedStore = StoreFixture.GENERAL_CLOSE_STORE();
// 			Table savedActiveTable = GENERAL_ACTIVE_TABLE(savedStore);
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			UserPassport queryUserPassport = UserFixture.GENERAL_USER_PASSPORT();
// 			Long queryTableId = savedActiveTable.getTableId();
// 			Long querySaleId = savedSale.getSaleId();
//
// 			BDDMockito.given(saleReader.readSingleSale(querySaleId))
// 				.willReturn(Optional.of(savedSale));
// 			BDDMockito.given(tableReader.findLockTableById(queryTableId, savedSale.getStore().getStoreId()))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.registerReceipt(queryTableId, querySaleId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TABLE);
//
// 				verify(saleReader).readSingleSale(querySaleId);
// 				verify(tableReader).findLockTableById(queryTableId, savedSale.getStore().getStoreId());
// 				verify(tableWriter, never()).changeTableActiveStatus(anyBoolean(), any());
// 				verify(receiptWriter, never()).createReceipt(any(), any());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("영수증 상세 조회")
// 	class getReceiptInfo {
// 		private final Long receiptId = GENERAL_RECEIPT_ID;
//
// 		@Test
// 		void 영수증_상세_조회_성공() {
// 			// given
// 			ReceiptInfo receiptInfo = ReceiptInfoFixture.NON_ADJUSTMENT_RECEIPT_INFO();
//
// 			BDDMockito.given(receiptReader.getReceiptInfo(receiptId))
// 				.willReturn(Optional.of(receiptInfo));
//
// 			// when
// 			receiptService.getReceiptInfo(receiptId);
//
// 			// then
// 			verify(receiptReader).getReceiptInfo(receiptId);
//
// 		}
//
// 		@Test
// 		void 영수증_조회_실패() {
// 			// given
// 			BDDMockito.given(receiptReader.getReceiptInfo(receiptId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.getReceiptInfo(receiptId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);
//
// 				verify(receiptReader).getReceiptInfo(receiptId);
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("영업 별 영수증 목록 조회")
// 	class getAdjustedReceiptPageBySale {
// 		private final int size = 10;
// 		private final Pageable pageable = Pageable.ofSize(size);
// 		private UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();
// 		private final Long saleId = 1L;
//
// 		@Test
// 		void 영수증_목록_조회_성공() {
// 			// given
// 			Store savedStore = StoreFixture.GENERAL_OPEN_STORE();
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			BDDMockito.given(saleReader.readSingleSale(saleId))
// 				.willReturn(Optional.of(savedSale));
//
// 			// when
// 			receiptService.getAdjustedReceiptPageBySale(pageable, userPassport, saleId);
//
// 			// then
// 			verify(saleReader).readSingleSale(saleId);
// 			verify(storeValidator).validateStoreOwner(userPassport, savedSale.getStore().getStoreId());
// 			verify(receiptReader).getAdjustedReceiptPageBySale(pageable, saleId);
// 		}
//
// 		@Test
// 		void 영업_조회_실패() {
// 			// given
// 			BDDMockito.given(saleReader.readSingleSale(saleId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.getAdjustedReceiptPageBySale(pageable, userPassport, saleId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SALE);
//
// 				verify(saleReader).readSingleSale(saleId);
// 				verify(storeValidator, never()).validateStoreOwner(any(), anyLong());
// 				verify(receiptReader, never()).getAdjustedReceiptPageBySale(pageable, saleId);
// 			});
// 		}
//
// 		@Test
// 		void 요청_유저_점주_불일치_실패() {
// 			// given
// 			userPassport = DIFF_OWNER_PASSPORT();
// 			Store savedStore = StoreFixture.GENERAL_OPEN_STORE();
// 			Sale savedSale = GENERAL_OPEN_SALE(savedStore);
//
// 			BDDMockito.given(saleReader.readSingleSale(saleId))
// 				.willReturn(Optional.of(savedSale));
// 			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
// 				.when(storeValidator).validateStoreOwner(userPassport, savedSale.getStore().getStoreId());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.getAdjustedReceiptPageBySale(pageable, userPassport, saleId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
//
// 				verify(saleReader).readSingleSale(saleId);
// 				verify(storeValidator).validateStoreOwner(userPassport, savedSale.getStore().getStoreId());
// 				verify(receiptReader, never()).getAdjustedReceiptPageBySale(pageable, saleId);
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("영수증 정산")
// 	class adjustReceipt {
// 		private final List<Long> receiptIds = List.of(1L);
// 		private UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();
//
// 		@Test
// 		void 영수증_정산_성공() {
// 			// given
// 			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
// 			List<Receipt> receipts = List.of(receipt);
// 			BDDMockito.given(receiptReader.getStopReceiptsWithTableAndStore(receiptIds))
// 				.willReturn(receipts);
//
// 			// when
// 			receiptService.adjustReceipts(receiptIds, userPassport);
//
// 			// then
// 			verify(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);
// 			verify(receiptValidator).validateIsOwner(receipt, userPassport);
// 			verify(tableWriter).changeTableActiveStatus(false, receipt.getTable());
// 			verify(receiptWriter).adjustReceipts(receipts);
// 		}
//
// 		@Test
// 		void 영수증_조회_실패() {
// 			// given
// 			BDDMockito.given(receiptReader.getStopReceiptsWithTableAndStore(receiptIds))
// 				.willReturn(new ArrayList<>());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.adjustReceipts(receiptIds, userPassport))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);
//
// 				verify(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);
// 				verify(receiptValidator, never()).validateIsOwner(any(), any());
// 				verify(receiptWriter, never()).adjustReceipts(anyList());
// 			});
// 		}
//
// 		@Test
// 		void 요청_유저_점주_불일치_실패() {
// 			// given
// 			userPassport = DIFF_OWNER_PASSPORT();
// 			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
// 			List<Receipt> receipts = List.of(receipt);
//
// 			BDDMockito.given(receiptReader.getStopReceiptsWithTableAndStore(receiptIds))
// 				.willReturn(receipts);
// 			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
// 				.when(receiptValidator).validateIsOwner(receipt, userPassport);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.adjustReceipts(receiptIds, userPassport))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);
//
// 				verify(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);
// 				verify(receiptValidator).validateIsOwner(receipt, userPassport);
// 				verify(receiptWriter, never()).adjustReceipts(anyList());
// 			});
// 		}
//
// 		@Test
// 		void 실패_정산된_영수증() {
// 			// given
// 			Receipt receipt = GENERAL_ADJUSTMENT_RECEIPT();
// 			List<Receipt> receipts = List.of(receipt);
//
// 			BDDMockito.given(receiptReader.getStopReceiptsWithTableAndStore(receiptIds))
// 				.willReturn(receipts);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.adjustReceipts(receiptIds, userPassport))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
//
// 				verify(receiptReader).getStopReceiptsWithTableAndStore(receiptIds);
// 				verify(receiptValidator).validateIsOwner(receipt, userPassport);
// 				verify(receiptWriter, never()).adjustReceipts(anyList());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("영수증 삭제")
// 	class deleteReceipt {
// 		private final Long receiptId = 1L;
// 		private UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();
//
// 		@Test
// 		void 영수증_삭제_성공() {
// 			// given
// 			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
//
// 			BDDMockito.given(receiptReader.getReceiptWithTableAndStore(receiptId))
// 				.willReturn(Optional.of(receipt));
//
// 			// when
// 			receiptService.deleteReceipt(receiptId, userPassport);
//
// 			// then
// 			verify(receiptReader).getReceiptWithTableAndStore(receiptId);
// 			verify(receiptValidator).validateIsOwner(receipt, userPassport);
// 			verify(receiptWriter).deleteReceipt(receiptId);
// 		}
//
// 		@Test
// 		void 영수증_조회_실패() {
// 			// given
// 			BDDMockito.given(receiptReader.getReceiptWithTableAndStore(receiptId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.deleteReceipt(receiptId, userPassport))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);
//
// 				verify(receiptReader).getReceiptWithTableAndStore(receiptId);
// 				verify(receiptValidator, never()).validateIsOwner(any(), any());
// 				verify(receiptWriter, never()).deleteReceipt(receiptId);
// 			});
// 		}
//
// 		@Test
// 		void 실패_요청_유저_점주_불일치() {
// 			// given
// 			userPassport = DIFF_OWNER_PASSPORT();
// 			Receipt receipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
//
// 			BDDMockito.given(receiptReader.getReceiptWithTableAndStore(receiptId))
// 				.willReturn(Optional.of(receipt));
// 			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
// 				.when(receiptValidator).validateIsOwner(receipt, userPassport);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> receiptService.deleteReceipt(receiptId, userPassport))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);
//
// 				verify(receiptReader).getReceiptWithTableAndStore(receiptId);
// 				verify(receiptValidator).validateIsOwner(any(), any());
// 				verify(receiptWriter, never()).deleteReceipt(receiptId);
// 			});
// 		}
// 	}
// }
