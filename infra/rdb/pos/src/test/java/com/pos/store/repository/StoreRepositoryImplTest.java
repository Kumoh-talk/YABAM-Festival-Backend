package com.pos.store.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreDetailImageFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import com.pos.fixtures.table.TableEntityFixture;
import com.pos.global.config.RepositoryTest;
import com.pos.receipt.ReceiptEntityFixture;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.port.required.StoreRepository;

class StoreRepositoryImplTest extends RepositoryTest {

	@Autowired
	private StoreRepository storeRepository;

	@Test
	void saveTest() {
		var store = STORE_FIXTURE();

		storeRepository.save(store);

		assertThat(store.getId()).isNotNull();
	}

	@Test
	void saveForUpdateTest() {
		var entity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		Store changed = DIFF_STORE_FIXTURE();
		ReflectionTestUtils.setField(changed, "id", entity.getId());

		storeRepository.save(changed);

		testEntityManager.flush();
		testEntityManager.clear();

		var findEntity = testEntityManager.find(StoreEntity.class, entity.getId());
		var findStore = StoreMapper.toStore(findEntity);

		assertThat(findStore.getStoreInfo()).isEqualTo(DIFF_STORE_FIXTURE().getStoreInfo());
		assertThat(findStore.getStoreInfo()).isNotEqualTo(GENERAL_CLOSE_STORE().getStoreInfo());
	}

	@Test
	void StoreInfo_변경_테스트() {
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		Store savedStore = StoreMapper.toStore(savedStoreEntity);
		testEntityManager.flush();
		testEntityManager.clear();

		StoreInfo changedStoreInfo = CHANGED_GENERAL_STORE().getStoreInfo();
		System.out.println("===StoreRepositoryImplTest.StoreInfo_변경_테스트 쿼리===");
		storeRepository.changeStoreInfo(savedStore, changedStoreInfo);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===StoreRepositoryImplTest.StoreInfo_변경_테스트 쿼리===");

		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity.getName()).isEqualTo(changedStoreInfo.getStoreName());
			softly.assertThat(findStoreEntity.getLocation()).isEqualTo(changedStoreInfo.getLocation());
			softly.assertThat(findStoreEntity.getDescription()).isEqualTo(changedStoreInfo.getDescription());
			softly.assertThat(findStoreEntity.getHeadImageUrl()).isEqualTo(changedStoreInfo.getThumbnailUrl());
			softly.assertThat(findStoreEntity.getUniversity()).isEqualTo(changedStoreInfo.getUniversityName());
			softly.assertThat(findStoreEntity.getTableCostPerTime().getTableCost())
				.isEqualTo(changedStoreInfo.getTableCost());
			softly.assertThat(findStoreEntity.getTableCostPerTime().getTableTime())
				.isEqualTo(changedStoreInfo.getTableTime());
		});
	}

	@Test
	void Store엔티티_논리_삭제_테스트() {
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();
		Store queryStore = StoreMapper.toStore(savedStoreEntity);

		System.out.println("===StoreRepositoryImplTest.Store엔티티_논리_삭제_테스트 쿼리===");
		storeRepository.deleteStore(queryStore);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===StoreRepositoryImplTest.Store엔티티_논리_삭제_테스트 쿼리===");

		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity).isNull();
		});
	}

	@Test
	void Store_오픈_상태_변경_테스트() {
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		Store opendStore = StoreMapper.toStore(savedStoreEntity).open();
		System.out.println("===StoreRepositoryImplTest.Store_오픈_상태_변경_테스트 쿼리===");
		storeRepository.changeStoreOpenStatus(opendStore);
		System.out.println("===StoreRepositoryImplTest.Store_오픈_상태_변경_테스트 쿼리===");

		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity.isOpen()).isFalse();
		});
	}

	@Test
	void store_존재여부_테스트() {
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		System.out.println("===StoreRepositoryImplTest.store_존재여부_테스트 쿼리===");
		boolean exists = storeRepository.isExistsById(savedStoreEntity.getId());
		boolean exists2 = storeRepository.isExistsById(savedStoreEntity.getId() + 1);
		System.out.println("===StoreRepositoryImplTest.store_존재여부_테스트 쿼리===");

		assertSoftly(softly -> {
			softly.assertThat(exists).isTrue();
			softly.assertThat(exists2).isFalse();
		});
	}

	@Test
	void 가게조회_테스트() {
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		List<StoreDetailImageEntity> storeDetailImageEntities = testFixtureBuilder.buildStoreDetailImageEntities(
			CUSTOM_STORE_DETAIL_IMAGES(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		System.out.println("===StoreRepositoryImplTest.가게조회_테스트 쿼리===");
		Store store = storeRepository.findStoreByStoreId(savedStoreEntity.getId()).get();
		System.out.println("===StoreRepositoryImplTest.가게조회_테스트 쿼리===");

		assertSoftly(softly -> {
			softly.assertThat(store.getId()).isEqualTo(savedStoreEntity.getId());
			softly.assertThat(store.getStoreInfo().getStoreName()).isEqualTo(savedStoreEntity.getName());
			softly.assertThat(store.getStoreInfo().getLocation()).isEqualTo(savedStoreEntity.getLocation());
			softly.assertThat(store.getStoreInfo().getDescription()).isEqualTo(savedStoreEntity.getDescription());
			softly.assertThat(store.getStoreInfo().getThumbnailUrl()).isEqualTo(savedStoreEntity.getHeadImageUrl());
			softly.assertThat(store.getStoreInfo().getUniversityName()).isEqualTo(savedStoreEntity.getUniversity());
			softly.assertThat(store.getStoreInfo().getTableCost())
				.isEqualTo(savedStoreEntity.getTableCostPerTime().getTableCost());
			softly.assertThat(store.getStoreInfo().getTableTime())
				.isEqualTo(savedStoreEntity.getTableCostPerTime().getTableTime());

			store.getDetailImageUrls()
				.forEach(url -> {
					softly.assertThat(storeDetailImageEntities.stream()
							.anyMatch(storeDetailImageEntity -> storeDetailImageEntity.getImageUrl().equals(url)))
						.isTrue();
				});

		});
	}

	@Test
	void 상세이미지_없을시_가게조회_테스트() {
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		System.out.println("===StoreRepositoryImplTest.가게조회_테스트 쿼리===");
		Store store = storeRepository.findStoreByStoreId(savedStoreEntity.getId()).get();
		System.out.println("===StoreRepositoryImplTest.가게조회_테스트 쿼리===");

		assertSoftly(softly -> {
			softly.assertThat(store.getId()).isEqualTo(savedStoreEntity.getId());
			softly.assertThat(store.getStoreInfo().getStoreName()).isEqualTo(savedStoreEntity.getName());
			softly.assertThat(store.getStoreInfo().getLocation()).isEqualTo(savedStoreEntity.getLocation());
			softly.assertThat(store.getStoreInfo().getDescription()).isEqualTo(savedStoreEntity.getDescription());
			softly.assertThat(store.getStoreInfo().getThumbnailUrl()).isEqualTo(savedStoreEntity.getHeadImageUrl());
			softly.assertThat(store.getStoreInfo().getUniversityName()).isEqualTo(savedStoreEntity.getUniversity());
			softly.assertThat(store.getStoreInfo().getTableCost())
				.isEqualTo(savedStoreEntity.getTableCostPerTime().getTableCost());
			softly.assertThat(store.getStoreInfo().getTableTime())
				.isEqualTo(savedStoreEntity.getTableCostPerTime().getTableTime());

		});
	}

	@Nested
	@DisplayName("가게 리스트 조회")
	class StoreListTest {
		private StoreEntity savedStoreEntity;
		private TableEntity savedTableEntity;
		private SaleEntity savedSaleEntity;
		private ReceiptEntity savedReceiptEntity;
		private List<StoreEntity> savedStoreEntityList;

		@BeforeEach
		void setUp() {
			savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_OPEN_STORE()));
			savedTableEntity = testFixtureBuilder.buildTableEntityList(
				TableEntityFixture.TABLEENTITY_LIST(1, savedStoreEntity)).get(0);
			savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
			savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
				ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));
			StoreEntity storeEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
			savedStoreEntityList = List.of(
				storeEntity, savedStoreEntity
			);
			testEntityManager.flush();
			testEntityManager.clear();
		}

		@Test
		void 가게_리스트_조회_테스트() {
			System.out.println("===StoreRepositoryImplTest.가게_리스트_조회_테스트 쿼리===");
			Slice<StoreHeadDto> storesCursorOrderByReviewCount = storeRepository.findStoresCursorOrderByCreated(
				null, 10);
			System.out.println("===StoreRepositoryImplTest.가게_리스트_조회_테스트 쿼리===");

			assertSoftly(softly -> {
				softly.assertThat(storesCursorOrderByReviewCount.getContent().size()).isEqualTo(2);
				for (int i = 0; i < storesCursorOrderByReviewCount.getContent().size(); i++) {
					StoreHeadDto storeHeadDto = storesCursorOrderByReviewCount.getContent().get(i);
					softly.assertThat(storeHeadDto.getStoreId()).isEqualTo(savedStoreEntityList.get(i).getId());
					softly.assertThat(storeHeadDto.getStoreName()).isEqualTo(savedStoreEntityList.get(i).getName());
					softly.assertThat(storeHeadDto.getIsOpened()).isEqualTo(savedStoreEntityList.get(i).isOpen());
					softly.assertThat(storeHeadDto.getThumbnailUrl())
						.isEqualTo(savedStoreEntityList.get(i).getHeadImageUrl());
				}
			});
		}
	}
}
