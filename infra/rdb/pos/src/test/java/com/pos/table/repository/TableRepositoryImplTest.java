package com.pos.table.repository;

import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.global.config.RepositoryTest;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.repository.TableRepository;

class TableRepositoryImplTest extends RepositoryTest {

	@Autowired
	private TableRepository tableRepository;

	private StoreEntity savedStoreEntity;
	private Store savedStore;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		savedStore = StoreMapper.toStore(savedStoreEntity);
		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Test
	void 테이블_수량_만큼_생성() {
		// given
		Integer tableCount = 5;

		// when
		System.out.println("===TableRepositoryImplTest.테이블_수량_만큼_생성 쿼리===");
		List<Table> tablesAll = tableRepository.createTablesAll(savedStore, tableCount);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===TableRepositoryImplTest.테이블_수량_만큼_생성 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(tablesAll).hasSize(tableCount);
			for (int i = 0; i < tableCount; i++) {
				softly.assertThat(tablesAll.get(i).getStore().getStoreId()).isEqualTo(savedStore.getStoreId());
				softly.assertThat(tablesAll.get(i).getTableNumber()).isEqualTo(i + 1);
				softly.assertThat(tablesAll.get(i).getTableId()).isNotNull();
			}
		});

	}

	@Test
	void EXISTS_락_조회_테스트() {
		// when
		System.out.println("===TableRepositoryImplTest.EXISTS_락_조회_테스트 쿼리===");
		boolean existsTable = tableRepository.existsTableByStoreWithLock(savedStore);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===TableRepositoryImplTest.EXISTS_락_조회_테스트 쿼리===");
		// then
		assertSoftly(softly -> {
			softly.assertThat(existsTable).isFalse();
		});
	}

	@Test
	void 테이블_아이디로_락_조회() {
		// given
		List<TableEntity> tableEntities = testFixtureBuilder
			.buildTableEntityList(TABLEENTITY_LIST(3, savedStoreEntity));

		// when
		System.out.println("===TableRepositoryImplTest.테이블_아이디로_락_조회 쿼리===");
		Table table = tableRepository.findByIdWithLock(tableEntities.get(0).getId(), savedStore.getStoreId()).get();
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===TableRepositoryImplTest.테이블_아이디로_락_조회 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(table.getTableId()).isEqualTo(tableEntities.get(0).getId());
			softly.assertThat(table.getTableNumber()).isEqualTo(tableEntities.get(0).getTableNumber().getTableNumber());
			softly.assertThat(table.getIsActive()).isFalse();
		});
	}

	@Nested
	@DisplayName("테이블 수 변경 테스트")
	class updateTableCount {
		@Test
		void 성공_테이블수보다_더많게_변경할수있다() {
			// given
			Integer previousCount = 5;
			Integer newCount = 7;
			List<Table> tablesAll = tableRepository.createTablesAll(savedStore, previousCount);
			testEntityManager.flush();
			testEntityManager.clear();

			// when
			System.out.println("===TableRepositoryImplTest.성공_테이블수보다_더많게_변경할수있다 쿼리===");
			List<Table> modifiedTables = tableRepository.updateTableNum(savedStore, newCount);
			testEntityManager.flush();
			testEntityManager.clear();
			System.out.println("===TableRepositoryImplTest.성공_테이블수보다_더많게_변경할수있다 쿼리===");

			// then
			assertSoftly(softly -> {
				List<Table> tables = tableRepository.findTablesByStoreId(savedStore.getStoreId());
				softly.assertThat(tables).hasSize(newCount);
				softly.assertThat(modifiedTables).hasSize(newCount);
				for (int i = 0; i < newCount; i++) {
					softly.assertThat(tables.get(i).getTableNumber()).isEqualTo(i + 1);
					softly.assertThat(tables.get(i).getIsActive()).isFalse();
					softly.assertThat(modifiedTables.get(i).getTableNumber()).isEqualTo(i + 1);
				}
			});
		}

		@Test
		void 성공_테이블수보다_적게_변경할수있다() {
			// given
			Integer previousCount = 5;
			Integer newCount = 3;
			List<Table> tablesAll = tableRepository.createTablesAll(savedStore, previousCount);
			testEntityManager.flush();
			testEntityManager.clear();

			// when
			System.out.println("===TableRepositoryImplTest.성공_테이블수보다_적게_변경할수있다 쿼리===");
			List<Table> modifiedTables = tableRepository.updateTableNum(savedStore, newCount);
			testEntityManager.flush();
			testEntityManager.clear();
			System.out.println("===TableRepositoryImplTest.성공_테이블수보다_적게_변경할수있다 쿼리===");

			// then
			assertSoftly(softly -> {
				List<Table> tables = tableRepository.findTablesByStoreId(savedStore.getStoreId());
				softly.assertThat(tables).hasSize(newCount);
				softly.assertThat(modifiedTables).hasSize(newCount);
				for (int i = 0; i < newCount; i++) {
					softly.assertThat(tables.get(i).getTableNumber()).isEqualTo(i + 1);
					softly.assertThat(tables.get(i).getIsActive()).isFalse();
					softly.assertThat(modifiedTables.get(i).getIsActive()).isFalse();
					softly.assertThat(modifiedTables.get(i).getTableNumber()).isEqualTo(i + 1);
				}
			});
		}

		@Test
		void 실패_테이블_수가_없으면_실패한다() {
			// given
			Integer newCount = 2;

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableRepository.updateTableNum(savedStore, newCount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.TABLE_NOT_FOUND);
			});
		}

		@Test
		void 실패_변경_테이블_수가_기존과_같으면_실패한다() {
			// given
			Integer previousCount = 5;
			Integer newCount = 5;
			tableRepository.createTablesAll(savedStore, previousCount);
			testEntityManager.flush();
			testEntityManager.clear();
			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> tableRepository.updateTableNum(savedStore, newCount))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.TABLE_NOT_EQUAL_MODIFY);
			});
		}
	}

}
