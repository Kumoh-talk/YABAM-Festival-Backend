package domain.pos.store.service;

import static fixtures.member.UserFixture.*;
import static fixtures.store.SaleFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.SaleReader;
import domain.pos.store.implement.SaleWriter;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.implement.StoreWriter;

class SaleServiceTest extends ServiceTest {
	@Mock
	private SaleWriter saleWriter;

	@Mock
	private SaleReader saleReader;

	@Mock
	private StoreWriter storeWriter;

	@Mock
	private ReceiptReader receiptReader;

	@Mock
	private StoreValidator storeValidator;

	@InjectMocks
	private SaleService saleService;

	@Nested
	@DisplayName("store 오픈")
	class openStore {
		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store savedStore = GENERAL_CLOSE_STORE();
			Store opendStore = GENERAL_OPEN_STORE();
			Long queryStoreId = savedStore.getStoreId();
			Sale createdSale = GENERAL_OPEN_SALE(opendStore);

			doReturn(savedStore)
				.when(storeValidator).validateStoreOwnerWithLock(queryUserPassport, queryStoreId);
			doReturn(opendStore)
				.when(storeWriter).modifyStoreOpenStatus(savedStore);
			doReturn(createdSale)
				.when(saleWriter).createSale(opendStore);
			// when
			Sale result = saleService.openStore(queryUserPassport, queryStoreId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isEqualTo(createdSale);
				softly.assertThat(result.getStore().getIsOpen()).isTrue();

				verify(storeValidator)
					.validateStoreOwnerWithLock(any(UserPassport.class), anyLong());
				verify(storeWriter)
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter)
					.createSale(any(Store.class));
			});
		}

		@Test
		void 실패_유효하지_않는_STORE_일때() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_USER_PASSPORT().getUserId();

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStoreOwnerWithLock(queryUserPassport, queryStoreId);

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.openStore(queryUserPassport, queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.NOT_FOUND_STORE.getMessage());
				verify(storeValidator)
					.validateStoreOwnerWithLock(any(UserPassport.class), anyLong());
				verify(storeWriter, never())
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter, never())
					.createSale(any(Store.class));
			});
		}

		@Test
		void 실패_가게주인과_요청유저가_다를때() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_USER_PASSPORT().getUserId();

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwnerWithLock(queryUserPassport, queryStoreId);

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.openStore(queryUserPassport, queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.NOT_EQUAL_STORE_OWNER.getMessage());
				verify(storeValidator)
					.validateStoreOwnerWithLock(any(UserPassport.class), anyLong());
				verify(storeWriter, never())
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter, never())
					.createSale(any(Store.class));
			});
		}

		@Test
		void 실패_이미_가게가_활성화되어있을때() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store savedStore = GENERAL_OPEN_STORE();
			Long queryStoreId = savedStore.getStoreId();

			doReturn(savedStore)
				.when(storeValidator).validateStoreOwnerWithLock(queryUserPassport, queryStoreId);

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.openStore(queryUserPassport, queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.CONFLICT_OPEN_STORE.getMessage());
				verify(storeValidator)
					.validateStoreOwnerWithLock(any(UserPassport.class), anyLong());
				verify(storeWriter, never())
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter, never())
					.createSale(any(Store.class));
			});
		}
	}

	@Nested
	@DisplayName("store 종료")
	class closeStore {
		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store savedStore = GENERAL_OPEN_STORE();
			Sale savedOpenedSale = GENERAL_OPEN_SALE(savedStore);
			Long querySaleId = savedOpenedSale.getSaleId();

			Store closedStore = GENERAL_CLOSE_STORE();
			Sale closedSale = GENERAL_CLOSE_SALE(closedStore);

			doReturn(Optional.of(savedOpenedSale))
				.when(saleReader).readSingleSale(querySaleId);
			doReturn(closedStore)
				.when(storeWriter).modifyStoreOpenStatus(savedOpenedSale.getStore());
			doReturn(closedSale)
				.when(saleWriter).closeSale(savedOpenedSale, closedStore);

			// when
			Sale result = saleService.closeStore(queryUserPassport, querySaleId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isEqualTo(closedSale);
				softly.assertThat(result.getStore().getIsOpen()).isFalse();

				verify(saleReader)
					.readSingleSale(anyLong());
				verify(storeWriter)
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter)
					.closeSale(any(Sale.class), any(Store.class));
			});

		}

		@Test
		void 실패_유효하지_않는_SALE_ID() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryDifferentSaleId = 999L;

			doReturn(Optional.empty())
				.when(saleReader).readSingleSale(queryDifferentSaleId);

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.closeStore(queryUserPassport, queryDifferentSaleId))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.NOT_FOUND_STORE.getMessage());
				verify(saleReader)
					.readSingleSale(anyLong());
				verify(storeWriter, never())
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter, never())
					.closeSale(any(Sale.class), any(Store.class));
			});
		}

		@Test
		void 실패_이미_종료된_SALE() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Sale savedClosedSale = GENERAL_CLOSE_SALE(GENERAL_CLOSE_STORE());

			doReturn(Optional.of(savedClosedSale))
				.when(saleReader).readSingleSale(savedClosedSale.getSaleId());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.closeStore(queryUserPassport, savedClosedSale.getSaleId()))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.CONFLICT_CLOSE_STORE.getMessage());
				verify(saleReader)
					.readSingleSale(anyLong());
				verify(storeWriter, never())
					.modifyStoreOpenStatus(any(Store.class));
				verify(saleWriter, never())
					.closeSale(any(Sale.class), any(Store.class));
			});
		}

		@Test
		void 실패_이미_종료된_가게_상태() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store savedStore = GENERAL_CLOSE_SALE(GENERAL_CLOSE_STORE()).getStore();
			Sale savedOpenedSale = GENERAL_OPEN_SALE(savedStore);
			Long querySaleId = savedOpenedSale.getSaleId();
			doReturn(Optional.of(savedOpenedSale))
				.when(saleReader).readSingleSale(querySaleId);

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> saleService.closeStore(queryUserPassport, querySaleId))
					.isInstanceOf(ServiceException.class)
					.hasMessage(ErrorCode.CONFLICT_CLOSE_STORE.getMessage());
			});

			verify(saleReader)
				.readSingleSale(anyLong());
			verify(storeWriter, never())
				.modifyStoreOpenStatus(any(Store.class));
			verify(saleWriter, never())
				.closeSale(any(Sale.class), any(Store.class));
		}
	}

}
