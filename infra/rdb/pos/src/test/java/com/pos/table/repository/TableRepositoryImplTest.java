package com.pos.table.repository;

import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.global.config.RepositoryTest;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
import domain.pos.table.port.required.repository.TableRepository;

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
	void 테이블_아이디로_락_조회() {
		// given
		List<TableEntity> tableEntities = testFixtureBuilder
			.buildTableEntityList(TABLEENTITY_LIST(3, savedStoreEntity));

		// when
		System.out.println("===TableRepositoryImplTest.테이블_아이디로_락_조회 쿼리===");
		Table table = tableRepository.findByIdWithLock(tableEntities.get(0).getId(), savedStore.getId()).get();
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===TableRepositoryImplTest.테이블_아이디로_락_조회 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(table.getId()).isEqualTo(tableEntities.get(0).getId());
			softly.assertThat(table.getTableNumber()).isEqualTo(tableEntities.get(0).getTableNumber().getTableNumber());
			softly.assertThat(table.getIsActive()).isFalse();
		});
	}

	@Nested
	@DisplayName("existsTableByStoreAndTableNumWithLock")
	class ExistsTableLock {

		@Test
		void false_테이블없음() {
			// when
			System.out.println("===TableRepositoryImplTest.existsTableByStoreAndTableNumWithLock 쿼리===");
			boolean exists = tableRepository.existsTableByStoreAndTableNumWithLock(savedStore, 1);
			System.out.println("===TableRepositoryImplTest.existsTableByStoreAndTableNumWithLock 쿼리===");
			// then
			assertSoftly(softly -> softly.assertThat(exists).isFalse());
		}

		@Test
		void true_테이블존재() {
			// given
			testFixtureBuilder.buildTableEntityList(TABLEENTITY_LIST(2, savedStoreEntity));
			testEntityManager.flush();
			testEntityManager.clear();
			// when
			System.out.println("===TableRepositoryImplTest.existsTableByStoreAndTableNumWithLock 쿼리===");
			boolean exists = tableRepository.existsTableByStoreAndTableNumWithLock(savedStore, 2);
			System.out.println("===TableRepositoryImplTest.existsTableByStoreAndTableNumWithLock 쿼리===");
			// then
			assertSoftly(softly -> softly.assertThat(exists).isTrue());
		}
	}

	@Test
	@DisplayName("saveTable – 저장 후 ID 반환")
	void saveTable() {
		// given
		Integer tableNum = 1;
		TablePoint point = TablePoint.of(3, 4);
		Integer tableCapacity = 4;
		// when
		System.out.println("===TableRepositoryImplTest.saveTable 쿼리===");
		UUID id = tableRepository.saveTable(savedStore, tableNum, point, tableCapacity);
		System.out.println("===TableRepositoryImplTest.saveTable 쿼리===");
		testEntityManager.flush();
		testEntityManager.clear();
		// then
		assertSoftly(softly -> {
			TableEntity entity = testEntityManager.find(TableEntity.class, id);
			softly.assertThat(entity).isNotNull();
			softly.assertThat(entity.getTableNumber().getTableNumber()).isEqualTo(tableNum);
			softly.assertThat(entity.getTablePoint().getTableX()).isEqualTo(3);
			softly.assertThat(entity.getTablePoint().getTableY()).isEqualTo(4);
		});
	}

	@Test
	@DisplayName("findTableWithStoreByTableId – 테이블과 가게 조인 조회")
	void findTableJoinStore() {
		// given
		TableEntity tableEntity = testFixtureBuilder.buildTableEntity(GENERAL_TABLE_ENTITY(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();
		// when
		System.out.println("===TableRepositoryImplTest.findTableJoinStore 쿼리===");
		Optional<Table> opt = tableRepository.findTableWithStoreByTableId(tableEntity.getId());
		System.out.println("===TableRepositoryImplTest.findTableJoinStore 쿼리===");
		// then
		assertSoftly(softly -> {
			softly.assertThat(opt).isPresent();
			Table table = opt.get();
			softly.assertThat(table.getId()).isEqualTo(tableEntity.getId());
			softly.assertThat(table.getStore().getId()).isEqualTo(savedStore.getId());
		});
	}

	@Test
	@DisplayName("updateTableInfo – 번호와 좌표 수정")
	void updateTableInfo() {
		// given
		TableEntity tableEntity = testFixtureBuilder.buildTableEntity(GENERAL_TABLE_ENTITY(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();
		Table table = TableMapper.toTable(tableEntity, savedStore);
		Integer newNumber = table.getTableNumber() + 2;
		TablePoint newPoint = TablePoint.of(7, 8);
		Integer newCapacity = table.getTableCapacity() + 1;
		// when
		System.out.println("===TableRepositoryImplTest.updateTableInfo 쿼리===");
		tableRepository.updateTableInfo(table, newNumber, newPoint, newCapacity);
		System.out.println("===TableRepositoryImplTest.updateTableInfo 쿼리===");
		testEntityManager.flush();
		testEntityManager.clear();
		// then
		assertSoftly(softly -> {
			TableEntity updated = testEntityManager.find(TableEntity.class, tableEntity.getId());
			softly.assertThat(updated.getTableNumber().getTableNumber()).isEqualTo(newNumber);
			softly.assertThat(updated.getTablePoint().getTableX()).isEqualTo(7);
			softly.assertThat(updated.getTablePoint().getTableY()).isEqualTo(8);
		});
	}

}
