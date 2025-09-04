package domain.pos.table.service;

import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.table.TableFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
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
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
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
			Long queryStoreId = GENERAL_CLOSE_STORE().getId();

			Store responStore = GENERAL_CLOSE_STORE();
			Table createdTable = GENERAL_IN_ACTIVE_TABLE(responStore);

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());
			doReturn(IS_NOT_EXISTS_TABLE)
				.when(tableReader).isExistsTableByStoreAndTableNumWithLock(any(Store.class), anyInt());
			doReturn(createdTable.getId())
				.when(tableWriter).createTable(any(Store.class), anyInt(), any(TablePoint.class), anyInt());
			// when
			UUID createdTableId = tableService.createTable(queryUserPassport, queryStoreId,
				createdTable.getTableNumber().value(), createdTable.getTablePoint(), createdTable.getTableCapacity());

			// then
			assertSoftly(softly -> {

				verify(storeValidator)
					.validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableReader)
					.isExistsTableByStoreAndTableNumWithLock(any(Store.class), anyInt());
				verify(tableWriter)
					.createTable(any(Store.class), anyInt(), any(TablePoint.class), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 가게가 운영 중이면 STORE_IS_OPEN_TABLE_WRITE")
		void 실패_가게_운영중() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_OPEN_STORE();
			Long queryStoreId = responStore.getId();
			Integer queryTableNum = GENERAL_ACTIVE_TABLE(responStore).getTableNumber().value();
			TablePoint queryTablePoint = GENERAL_ACTIVE_TABLE(responStore).getTablePoint();
			Integer queryTableCapacity = GENERAL_ACTIVE_TABLE(responStore).getTableCapacity();

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(
							queryUserPassport,
							queryStoreId,
							queryTableNum,
							queryTablePoint,
							queryTableCapacity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_TABLE_WRITE);

				verify(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(Store.class), anyInt());
				verify(tableWriter, never()).createTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 이미 존재하는 테이블이면 EXIST_TABLE")
		void 실패_이미_존재하는_테이블() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();  // isOpen = false
			Long queryStoreId = responStore.getId();
			Integer queryTableNum = GENERAL_ACTIVE_TABLE(responStore).getTableNumber().value();
			TablePoint queryTablePoint = GENERAL_ACTIVE_TABLE(responStore).getTablePoint();
			Integer queryTableCapacity = GENERAL_ACTIVE_TABLE(responStore).getTableCapacity();

			doReturn(responStore)
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());
			doReturn(IS_EXISTS_TABLE)
				.when(tableReader).isExistsTableByStoreAndTableNumWithLock(any(Store.class), anyInt());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(
							queryUserPassport,
							queryStoreId,
							queryTableNum,
							queryTablePoint,
							queryTableCapacity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_TABLE);

				verify(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableReader).isExistsTableByStoreAndTableNumWithLock(any(Store.class), anyInt());
				verify(tableWriter, never()).createTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 유효하지 않은 가게 ID면 NOT_FOUND_STORE")
		void 실패_유효하지_않은_가게_ID() {
			// given
			UserPassport queryUserPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();  // isOpen = false
			Long queryStoreId = responStore.getId();
			Integer queryTableNum = GENERAL_ACTIVE_TABLE(responStore).getTableNumber().value();
			TablePoint queryTablePoint = GENERAL_ACTIVE_TABLE(responStore).getTablePoint();
			Integer queryTableCapacity = GENERAL_ACTIVE_TABLE(responStore).getTableCapacity();

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(
							queryUserPassport,
							queryStoreId,
							queryTableNum,
							queryTablePoint,
							queryTableCapacity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

				verify(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(), anyInt());
				verify(tableWriter, never()).createTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 점주 ID 불일치면 NOT_EQUAL_STORE_OWNER")
		void 실패_점주_ID_불일치() {
			// given
			UserPassport diffOwnerPassport = DIFF_OWNER_PASSPORT();
			Long queryStoreId = GENERAL_CLOSE_STORE().getId();

			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(any(UserPassport.class), anyLong());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableService.createTable(
							diffOwnerPassport,
							queryStoreId,
							GENERAL_ACTIVE_TABLE(GENERAL_CLOSE_STORE()).getTableNumber().value(),
							GENERAL_ACTIVE_TABLE(GENERAL_CLOSE_STORE()).getTablePoint(),
							GENERAL_ACTIVE_TABLE(GENERAL_CLOSE_STORE()).getTableCapacity()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

				verify(storeValidator).validateStoreOwner(diffOwnerPassport, queryStoreId);
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(), anyInt());
				verify(tableWriter, never()).createTable(any(), anyInt(), any(), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("가게 테이블 업데이트")
	class updateTable {

		private static final boolean IS_EXISTS_TABLE = true;
		private static final boolean IS_NOT_EXISTS_TABLE = false;

		@Test
		@DisplayName("성공")
		void 성공() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			UUID queryTableId = savedTable.getId();
			Integer updateTableNumber = savedTable.getTableNumber().value() + 1;
			TablePoint updateTablePoint = savedTable.getTablePoint();
			Integer updateTableCapacity = savedTable.getTableCapacity() + 1;

			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);
			doReturn(IS_NOT_EXISTS_TABLE)
				.when(tableReader).isExistsTableByStoreAndTableNumWithLock(responStore, updateTableNumber);

			// when
			tableService.updateTable(
				ownerPassport,
				queryTableId,
				updateTableNumber,
				updateTablePoint,
				updateTableCapacity);

			// then
			assertSoftly(softly -> {
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableReader).isExistsTableByStoreAndTableNumWithLock(responStore, updateTableNumber);
				verify(tableWriter).updateTable(savedTable, updateTableNumber, updateTablePoint, updateTableCapacity);
			});
		}

		@Test
		@DisplayName("실패 – 테이블이 없으면 NOT_FOUND_TABLE")
		void 실패_테이블_없음() {
			// given
			UUID invalidTableId = UUID.randomUUID();
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			doReturn(Optional.empty())
				.when(tableReader).findTableWithStoreByTableId(invalidTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.updateTable(
						ownerPassport,
						invalidTableId,
						1,
						TablePoint.of(0, 0),
						4))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TABLE);
				verify(tableReader).findTableWithStoreByTableId(invalidTableId);
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(), anyInt());
				verify(tableWriter, never()).updateTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 점주 ID 불일치면 NOT_EQUAL_STORE_OWNER")
		void 실패_점주_ID_불일치() {
			// given
			UserPassport diffOwnerPassport = DIFF_OWNER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			UUID queryTableId = savedTable.getId();
			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.updateTable(
						diffOwnerPassport,
						queryTableId,
						savedTable.getTableNumber().value(),
						savedTable.getTablePoint(),
						savedTable.getTableCapacity()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(), anyInt());
				verify(tableWriter, never()).updateTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 가게가 운영 중이면 STORE_IS_OPEN_TABLE_WRITE")
		void 실패_가게_운영중() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Store openStore = GENERAL_OPEN_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(openStore);
			UUID queryTableId = savedTable.getId();
			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.updateTable(
						ownerPassport,
						queryTableId,
						savedTable.getTableNumber().value(),
						savedTable.getTablePoint(),
						savedTable.getTableCapacity()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableReader, never()).isExistsTableByStoreAndTableNumWithLock(any(), anyInt());
				verify(tableWriter, never()).updateTable(any(), anyInt(), any(), anyInt());
			});
		}

		@Test
		@DisplayName("실패 – 수정하려는 번호가 기존과 다르고 이미 존재하면 EXIST_TABLE")
		void 실패_이미_존재하는_테이블번호() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			UUID queryTableId = savedTable.getId();
			Integer updateTableNumber = savedTable.getTableNumber().value() + 1;
			TablePoint updatePoint = savedTable.getTablePoint();
			Integer updateTableCapacity = savedTable.getTableCapacity() + 1;

			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);
			doReturn(IS_EXISTS_TABLE)
				.when(tableReader).isExistsTableByStoreAndTableNumWithLock(responStore, updateTableNumber);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.updateTable(
						ownerPassport,
						queryTableId,
						updateTableNumber,
						updatePoint,
						updateTableCapacity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_TABLE);
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableReader).isExistsTableByStoreAndTableNumWithLock(responStore, updateTableNumber);
				verify(tableWriter, never()).updateTable(any(), anyInt(), any(), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("가게 테이블 삭제")
	class deleteTable {

		@Test
		@DisplayName("성공")
		void 성공() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			UUID queryTableId = savedTable.getId();
			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);

			// when
			tableService.deleteTable(ownerPassport, queryTableId);

			// then
			assertSoftly(softly -> {
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableWriter).deleteTable(savedTable);
			});
		}

		@Test
		@DisplayName("실패 – 테이블이 없으면 NOT_FOUND_TABLE")
		void 실패_테이블_없음() {
			// given
			UUID invalidTableId = UUID.randomUUID();
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			doReturn(Optional.empty())
				.when(tableReader).findTableWithStoreByTableId(invalidTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.deleteTable(ownerPassport, invalidTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TABLE);
				verify(tableReader).findTableWithStoreByTableId(invalidTableId);
				verify(tableWriter, never()).deleteTable(any());
			});
		}

		@Test
		@DisplayName("실패 – 점주 ID 불일치면 NOT_EQUAL_STORE_OWNER")
		void 실패_점주_ID_불일치() {
			// given
			UserPassport diffOwnerPassport = DIFF_OWNER_PASSPORT();
			Store responStore = GENERAL_CLOSE_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(responStore);
			UUID queryTableId = savedTable.getId();
			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.deleteTable(diffOwnerPassport, queryTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableWriter, never()).deleteTable(any());
			});
		}

		@Test
		@DisplayName("실패 – 가게가 운영 중이면 STORE_IS_OPEN_TABLE_WRITE")
		void 실패_가게_운영중() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();
			Store openStore = GENERAL_OPEN_STORE();
			Table savedTable = GENERAL_IN_ACTIVE_TABLE(openStore);
			UUID queryTableId = savedTable.getId();
			doReturn(Optional.of(savedTable))
				.when(tableReader).findTableWithStoreByTableId(queryTableId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> tableService.deleteTable(ownerPassport, queryTableId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
				verify(tableReader).findTableWithStoreByTableId(queryTableId);
				verify(tableWriter, never()).deleteTable(any());
			});
		}
	}

	@Nested
	@DisplayName("가게 테이블 활성화 여부 리스트 조회")
	class findTables {
		@Test
		void 성공() {
			// given
			Long queryStoreId = GENERAL_OPEN_STORE().getId();

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
