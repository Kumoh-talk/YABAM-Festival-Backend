package com.pos.store.repository;

import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.global.config.RepositoryTest;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.repository.StoreRepository;

class StoreRepositoryImplTest extends RepositoryTest {

	@Autowired
	private StoreRepository storeRepository;

	@Test
	void StoreInfo_변경_테스트() {
		// given
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		StoreInfo changedStoreInfo = CHANGED_GENERAL_STORE().getStoreInfo();
		// when
		System.out.println("===StoreRepositoryImplTest.StoreInfo_변경_테스트 쿼리===");
		storeRepository.changeStoreInfo(savedStore, changedStoreInfo);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===StoreRepositoryImplTest.StoreInfo_변경_테스트 쿼리===");

		// then
		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity.getName()).isEqualTo(changedStoreInfo.getStoreName());
			softly.assertThat(findStoreEntity.getLocation().getLatitude()).isEqualTo(changedStoreInfo.getLocation().x);
			softly.assertThat(findStoreEntity.getLocation().getLongitude()).isEqualTo(changedStoreInfo.getLocation().y);
			softly.assertThat(findStoreEntity.getDescription()).isEqualTo(changedStoreInfo.getDescription());
			softly.assertThat(findStoreEntity.getHeadImageUrl()).isEqualTo(changedStoreInfo.getHeadImageUrl());
			softly.assertThat(findStoreEntity.getUniversity()).isEqualTo(changedStoreInfo.getUniversity());
			softly.assertThat(findStoreEntity.getTableCostPerTime().getTableCost())
				.isEqualTo(changedStoreInfo.getTableCost());
			softly.assertThat(findStoreEntity.getTableCostPerTime().getTableTime())
				.isEqualTo(changedStoreInfo.getTableTime());
		});
	}

	@Test
	void Store엔티티_논리_삭제_테스트() {
		// given
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();
		Store queryStore = StoreMapper.toStore(savedStoreEntity);

		// when
		System.out.println("===StoreRepositoryImplTest.Store엔티티_논리_삭제_테스트 쿼리===");
		storeRepository.deleteStore(queryStore);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===StoreRepositoryImplTest.Store엔티티_논리_삭제_테스트 쿼리===");

		// then
		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity).isNull();
		});
	}

	@Test
	void Store_오픈_상태_변경_테스트() {
		// given
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		Store opendStore = StoreMapper.toStore(savedStoreEntity).open();
		// when
		System.out.println("===StoreRepositoryImplTest.Store_오픈_상태_변경_테스트 쿼리===");
		storeRepository.changeStoreOpenStatus(opendStore);
		System.out.println("===StoreRepositoryImplTest.Store_오픈_상태_변경_테스트 쿼리===");

		// then
		assertSoftly(softly -> {
			StoreEntity findStoreEntity = testEntityManager.find(StoreEntity.class, savedStoreEntity.getId());
			softly.assertThat(findStoreEntity.isOpen()).isTrue();
		});
	}

	@Test
	void store_존재여부_테스트() {
		// given
		Store savedStore = GENERAL_CLOSE_STORE();
		StoreEntity savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(savedStore));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===StoreRepositoryImplTest.store_존재여부_테스트 쿼리===");
		boolean exists = storeRepository.isExistsById(savedStoreEntity.getId());
		boolean exists2 = storeRepository.isExistsById(savedStoreEntity.getId() + 1);
		System.out.println("===StoreRepositoryImplTest.store_존재여부_테스트 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(exists).isTrue();
			softly.assertThat(exists2).isFalse();
		});
	}
}
