package com.pos.store.repository.port;

import static com.pos.fixtures.store.StoreDetailImageFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.DetailImagesFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.pos.global.config.RepositoryTest;

class DetailImageRepositoryImplTest extends RepositoryTest {

	@Autowired
	DetailImageRepositoryImpl detailImageRepository;

	@Autowired
	TestEntityManager em;

	@Test
	void findByStoreIdTest() {
		var storeEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		var storeDetailImageEntities = testFixtureBuilder.buildStoreDetailImageEntities(
			CUSTOM_STORE_DETAIL_IMAGES(storeEntity));
		em.flush();
		em.clear();

		var result = detailImageRepository.findByStoreId(storeEntity.getId());

		assertThat(result.getImageUrls().size()).isEqualTo(storeDetailImageEntities.size());
		assertThat(result.getStoreId()).isEqualTo(storeEntity.getId());
	}

	@Test
	void saveTest() {
		var storeEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		var storeDetailImageEntities = testFixtureBuilder.buildStoreDetailImageEntities(
			CUSTOM_STORE_DETAIL_IMAGES(storeEntity));
		em.flush();
		em.clear();

		var changedDetailImage = DIFF_IMAGE_FIXTURE(storeEntity.getId());

		// when
		detailImageRepository.save(changedDetailImage);
		em.flush();
		em.clear();

		var result = detailImageRepository.findByStoreId(storeEntity.getId());

		assertThat(result.getImageUrls().size()).isEqualTo(changedDetailImage.getImageUrls().size());
		assertThat(result.getImageUrls().get(0)).contains(changedDetailImage.getImageUrls());

		assertThat(result.getStoreId()).isEqualTo(storeEntity.getId());
	}

}
