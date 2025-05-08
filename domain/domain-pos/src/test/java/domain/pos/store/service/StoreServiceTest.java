package domain.pos.store.service;

import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.store.StoreInfoFixture.*;
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
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.implement.StoreWriter;

class StoreServiceTest extends ServiceTest {

	@Mock
	private StoreValidator storeValidator;

	@Mock
	private StoreWriter storeWriter;

	@Mock
	private StoreReader storeReader;

	@InjectMocks
	private StoreService storeService;

	@Nested
	@DisplayName("store 생성")
	class createStoreInfo {
		private static final Long SAVED_STORE_ID = 1L;

		@Test
		void 성공() {
			// given
			StoreInfo requestStoreInfo = GENERAL_STORE_INFO();
			UserPassport userPassport = OWNER_USER_PASSPORT();

			doReturn(SAVED_STORE_ID)
				.when(storeWriter).createStore(userPassport, requestStoreInfo);

			// when
			Long storeId = storeService.createStore(userPassport, requestStoreInfo);
			// then
			assertSoftly(softly -> {
				softly.assertThat(storeId).isEqualTo(SAVED_STORE_ID);

				verify(storeWriter)
					.createStore(any(UserPassport.class), any(StoreInfo.class));
			});
		}

	}

	@Nested
	@DisplayName("가게 단건 조회")
	class singleSearchStore {
		@Test
		void 성공() {
			// given
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Store responseStore = GENERAL_CLOSE_STORE();

			doReturn(Optional.of(responseStore))
				.when(storeReader).readSingleStore(queryStoreId);

			// when
			Store savedStore = storeService.findStore(queryStoreId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(responseStore).isEqualTo(savedStore);

				verify(storeReader)
					.readSingleStore(queryStoreId);
			});
		}

		@Test
		void 실패_유효하지_않은_가게_ID() {
			// given
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();

			doReturn(Optional.empty())
				.when(storeReader).readSingleStore(queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> storeService.findStore(queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

				verify(storeReader)
					.readSingleStore(queryStoreId);
			});
		}
	}

	@Nested
	@DisplayName("가게 수정")
	class updateStore {
		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Store nonChangedStore = GENERAL_CLOSE_STORE();
			StoreInfo requestChangeStoreInfo = CHANGED_GENERAL_STORE().getStoreInfo();
			Store changedStore = CHANGED_GENERAL_STORE();

			doReturn(nonChangedStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);
			doReturn(changedStore)
				.when(storeWriter).updateStoreInfo(nonChangedStore, requestChangeStoreInfo);

			// when
			Store result = storeService.updateStoreInfo(
				queryUserPassport,
				queryStoreId,
				requestChangeStoreInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result.getStoreId()).isEqualTo(changedStore.getStoreId());

				verify(storeValidator).validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter).updateStoreInfo(any(Store.class), any(StoreInfo.class));
			});
		}

		@Test
		void 실패_수정_요청자가_가게_OWNER와_다를시() {
			// given
			UserPassport diffOwnerUserPassport = DIFF_OWNER_PASSPORT();
			Store previousStore = GENERAL_CLOSE_STORE();
			Long queryStoreId = previousStore.getStoreId();
			StoreInfo requestChangeStoreInfo = CHANGED_GENERAL_STORE().getStoreInfo();

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(diffOwnerUserPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> storeService.updateStoreInfo(
						diffOwnerUserPassport,
						queryStoreId,
						requestChangeStoreInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter, never())
					.updateStoreInfo(any(Store.class), any(StoreInfo.class));
			});

		}

		@Test
		void 실패_유효하지_않는_가게_ID() {
			// given
			UserPassport queryOwnerPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			StoreInfo requestChangeStoreInfo = CHANGED_GENERAL_STORE().getStoreInfo();

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStoreOwner(queryOwnerPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> storeService.updateStoreInfo(
						queryOwnerPassport,
						queryStoreId,
						requestChangeStoreInfo
					))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter, never())
					.updateStoreInfo(any(Store.class), any(StoreInfo.class));
			});
		}
	}

	@Nested
	@DisplayName("가게 삭제시")
	class deleteStore {
		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Store savedStore = GENERAL_CLOSE_STORE();

			doReturn(savedStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when
			storeService.deleteStore(queryUserPassport, queryStoreId);

			// then
			assertSoftly(softly -> {
				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter)
					.deleteStore(any(Store.class));
			});
		}

		@Test
		void 실패_유효하지_않는_가게_ID() {
			// given
			UserPassport queryOwnerPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStoreOwner(queryOwnerPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> storeService.deleteStore(queryOwnerPassport, queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter, never())
					.deleteStore(any(Store.class));
			});
		}

		@Test
		void 실패_가게ID와_점주ID가_다를시() {
			// given
			UserPassport queryDiffOwnerPassport = DIFF_OWNER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(queryDiffOwnerPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> storeService.deleteStore(queryDiffOwnerPassport, queryStoreId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), any(Long.class));
				verify(storeWriter, never())
					.deleteStore(any(Store.class));
			});
		}
	}
}
