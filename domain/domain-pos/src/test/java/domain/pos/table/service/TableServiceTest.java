package domain.pos.table.service;

import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.table.TableFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;

class TableServiceTest extends ServiceTest {

	@Mock
	private StoreValidator storeValidator;

	@Mock
	private TableReader tableReader;

	@Mock
	private TableWriter tableWriter;

	@InjectMocks
	private TableService tableService;

	@Nested
	@DisplayName("가게 테이블 생성")
	class createTable {
		private static final boolean IS_EXISTS_TABLE = true;
		private static final boolean IS_NOT_EXISTS_TABLE = false;

		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Integer queryTableNumber = 1;

			Store responStore = GENERAL_CLOSE_STORE();
			Table createdTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			List<Table> createdTables = List.of(createdTable);

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);
			doReturn(IS_NOT_EXISTS_TABLE)
				.when(tableReader).isExistsTable(responStore);
			doReturn(createdTables)
				.when(tableWriter).createTables(responStore, queryTableNumber);
			// when
			List<Table> resultTable = tableService.createTable(queryUserPassport, queryStoreId, queryTableNumber);

			// then
			assertSoftly(softly -> {
				softly.assertThat(resultTable).hasSize(queryTableNumber);

				verify(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);
				verify(tableReader)
					.isExistsTable(responStore);
				verify(tableWriter).createTables(responStore, queryTableNumber);
			});
		}

		@Test
		void 실패_운영중인_가게에서_테이블_생성불가() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_OPEN_STORE().getStoreId();
			Integer queryTableNumber = 1;

			Store responStore = GENERAL_OPEN_STORE();

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when -> then
			assertSoftly(
				softly -> {
					softly.assertThatThrownBy(
							() -> tableService.createTable(queryUserPassport, queryStoreId, queryTableNumber))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_TABLE_WRITE);

					verify(storeValidator)
						.validateStoreOwner(any(UserPassport.class), anyLong());
					verify(tableReader, never())
						.isExistsTable(any(Store.class));
					verify(tableWriter, never())
						.createTables(any(Store.class), anyInt());
				}
			);
		}

		@Test
		void 실패_유효하지_않은_storeId() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = 0L;
			Integer queryTableNumber = 1;

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(queryUserPassport, queryStoreId, queryTableNumber))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

				verify(storeValidator)
					.validateStoreOwner(queryUserPassport, queryStoreId);
				verify(tableReader, never())
					.isExistsTable(any(Store.class));
				verify(tableWriter, never())
					.createTables(any(Store.class), anyInt());
			});
		}

		@Test
		void 실패_점주_주인과_요청유저가_불일치() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Integer queryTableNumber = 1;

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(queryUserPassport, queryStoreId, queryTableNumber))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(storeValidator)
					.validateStoreOwner(queryUserPassport, queryStoreId);
				verify(tableReader, never())
					.isExistsTable(any(Store.class));
				verify(tableWriter, never())
					.createTables(any(Store.class), anyInt());
			});
		}

		@Test
		void 실패_존재하는_테이블() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Integer queryTableNumber = GENERAL_IN_ACTIVE_TABLE(GENERAL_CLOSE_STORE()).getTableNumber();

			Store responStore = GENERAL_CLOSE_STORE();
			Table createdTable = GENERAL_IN_ACTIVE_TABLE(responStore);

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);
			doReturn(IS_EXISTS_TABLE)
				.when(tableReader).isExistsTable(responStore);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(queryUserPassport, queryStoreId, queryTableNumber))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_TABLE);

				verify(storeValidator)
					.validateStoreOwner(queryUserPassport, queryStoreId);
				verify(tableReader)
					.isExistsTable(responStore);
				verify(tableWriter, never())
					.createTables(responStore, queryTableNumber);
			});
		}
	}

	@Nested
	@DisplayName("가게 테이블 수 조정")
	class updateTable {
		private static final boolean IS_EXISTS_TABLE = true;
		private static final boolean IS_NOT_EXISTS_TABLE = false;

		@Test
		void 성공() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getStoreId();
			Integer queryUpdateTableNumber = 2;

			Store responStore = GENERAL_CLOSE_STORE();
			Table createTable1 = GENERAL_IN_ACTIVE_TABLE(responStore);
			Table createTable2 = GENERAL_IN_ACTIVE_TABLE(responStore);
			List<Table> createdTables = List.of(
				createTable1,
				createTable2);

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when
			tableService.updateTableNum(queryUserPassport, queryStoreId,
				queryUpdateTableNumber);

			// then
			assertSoftly(softly -> {
				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableWriter)
					.modifyTableNum(any(Store.class), anyInt());

			});
		}

		@Test
		void 실패_운영중인_가게에서_테이블_수_조정불가() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = GENERAL_OPEN_STORE().getStoreId();
			Integer queryUpdateTableNumber = 2;

			Store responStore = GENERAL_OPEN_STORE();

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.updateTableNum(queryUserPassport, queryStoreId, queryUpdateTableNumber))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_TABLE_WRITE);

				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableWriter, never())
					.modifyTableNum(any(Store.class), anyInt());
			});
		}

		@Test
		void 실패_점주가_아닌_사용자() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Long queryStoreId = 0L;
			Integer queryUpdateTableNumber = 2;

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(queryUserPassport, queryStoreId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.updateTableNum(queryUserPassport, queryStoreId, queryUpdateTableNumber))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(storeValidator)
					.validateStoreOwner(queryUserPassport, queryStoreId);
				verify(tableWriter, never())
					.modifyTableNum(any(Store.class), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("가게 테이블 활성화 여부 리스트 조회")
	class findTables {
		@Test
		void 성공() {
			// given
			Long queryStoreId = GENERAL_OPEN_STORE().getStoreId();

			Store responStore = GENERAL_OPEN_STORE();
			Table savedTable1 = GENERAL_IN_ACTIVE_TABLE(responStore);
			Table savedTable2 = GENERAL_ACTIVE_TABLE(responStore);
			List<Table> savedTables = List.of(
				savedTable1,
				savedTable2);

			doReturn(savedTables)
				.when(tableReader).findTables(queryStoreId);

			// when
			List<Table> resultTables = tableService.findTables(queryStoreId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(resultTables).hasSize(2);
				softly.assertThat(resultTables).contains(savedTable1, savedTable2);

				verify(tableReader)
					.findTables(anyLong());
			});
		}
	}
}
