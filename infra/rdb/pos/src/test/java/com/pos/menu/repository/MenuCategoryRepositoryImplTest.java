package com.pos.menu.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.fixtures.menu.MenuCategoryEntityFixture;
import com.pos.fixtures.store.StoreEntityFixture;
import com.pos.global.config.RepositoryTest;
import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.mapper.MenuCategoryMapper;
import com.pos.menu.repository.jpa.MenuCategoryJpaRepository;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.store.entity.Store;
import fixtures.menu.MenuCategoryFixture;
import fixtures.menu.MenuCategoryInfoFixture;

class MenuCategoryRepositoryImplTest extends RepositoryTest {
	@Autowired
	private MenuCategoryRepository menuCategoryRepository;
	@Autowired
	private MenuCategoryJpaRepository menuCategoryJpaRepository;

	private Store savedStore;

	@BeforeEach
	void setUp() {
		StoreEntity storeEntity = testFixtureBuilder.buildStoreEntity(
			StoreEntityFixture.CUSTOME_STORE_ENTITY(MenuCategoryFixture.GENERAL_STORE));
		savedStore = StoreMapper.toStore(storeEntity);

		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Test
	void 카테고리_저장_성공() {
		MenuCategoryInfo menuCategoryInfo = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();

		MenuCategory menuCategory = menuCategoryRepository.postMenuCategory(savedStore, menuCategoryInfo);
		testEntityManager.flush();
		testEntityManager.clear();

		assertThat(menuCategoryJpaRepository.findById(menuCategory.getMenuCategoryInfo().getId())).isPresent();
		assertThat(menuCategory.getStore()).isEqualTo(savedStore);
		assertThat(menuCategory.getMenuCategoryInfo().getName()).isEqualTo(menuCategoryInfo.getName());
		assertThat(menuCategory.getMenuCategoryInfo().getOrder()).isEqualTo(menuCategoryInfo.getOrder());
	}

	@Test
	void 카테고리_정보_조회_성공() {
		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(), savedStore)));
		MenuCategory menuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		MenuCategoryInfo found = menuCategoryRepository.getMenuCategoryInfo(
			menuCategory.getStore().getId(),
			menuCategory.getMenuCategoryInfo().getId()).get();

		assertThat(found.getId()).isEqualTo(menuCategory.getMenuCategoryInfo().getId());
		assertThat(found.getName()).isEqualTo(menuCategory.getMenuCategoryInfo().getName());
		assertThat(found.getOrder()).isEqualTo(menuCategory.getMenuCategoryInfo().getOrder());
	}

	@Test
	void 카테고리_정보_리스트_조회_성공() {
		MenuCategoryEntity firstEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
		MenuCategoryEntity secondEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
		testEntityManager.flush();
		testEntityManager.clear();

		List<MenuCategoryInfo> found = menuCategoryRepository.getMenuCategoryInfoList(savedStore.getId());

		assertThat(found).hasSize(2);
		assertThat(found.get(0).getId()).isEqualTo(firstEntity.getId());
		assertThat(found.get(1).getId()).isEqualTo(secondEntity.getId());
	}

	@Test
	void 카테고리_정보_수정_성공() {
		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(), savedStore)));
		MenuCategory menuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		Long savedId = menuCategory.getMenuCategoryInfo().getId();
		MenuCategoryInfo patchInfo = MenuCategoryInfo.of(savedId,
			MenuCategoryInfoFixture.DIFF_MENU_CATEGORY_NAME, MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_ORDER);

		MenuCategoryInfo saved = menuCategoryRepository.patchMenuCategory(patchInfo);

		assertThat(saved.getId()).isEqualTo(menuCategory.getMenuCategoryInfo().getId());
		assertThat(saved.getOrder()).isEqualTo(menuCategory.getMenuCategoryInfo().getOrder());
		assertThat(saved.getName()).isEqualTo(patchInfo.getName());
	}

	@Nested
	@DisplayName("메뉴 카테고리 순서 수정")
	class patchMenuCategoryOrder {
		private MenuCategory firstCategory;
		private MenuCategory secondCategory;

		@BeforeEach
		void setUp() {
			MenuCategoryEntity firstEntity = testFixtureBuilder.buildMenuCategoryEntity(
				MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
					MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
			MenuCategoryEntity secondEntity = testFixtureBuilder.buildMenuCategoryEntity(
				MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
					MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
			firstCategory = MenuCategoryMapper.toMenuCategory(firstEntity, savedStore);
			secondCategory = MenuCategoryMapper.toMenuCategory(secondEntity, savedStore);
			testEntityManager.flush();
			testEntityManager.clear();
		}

		@Test
		void 순서_증가_성공() {
			Integer patchOrder = secondCategory.getMenuCategoryInfo().getOrder();

			MenuCategoryInfo saved = menuCategoryRepository.patchMenuCategoryOrder(
				savedStore.getId(), firstCategory.getMenuCategoryInfo(), patchOrder);

			assertThat(saved.getId()).isEqualTo(firstCategory.getMenuCategoryInfo().getId());
			assertThat(saved.getOrder()).isEqualTo(patchOrder);
			assertThat(menuCategoryJpaRepository.findById(secondCategory.getMenuCategoryInfo().getId())
				.get().getOrder()).isEqualTo(firstCategory.getMenuCategoryInfo().getOrder());
		}

		@Test
		void 순서_감소_성공() {
			Integer patchOrder = firstCategory.getMenuCategoryInfo().getOrder();

			MenuCategoryInfo saved = menuCategoryRepository.patchMenuCategoryOrder(
				savedStore.getId(), secondCategory.getMenuCategoryInfo(), patchOrder);

			assertThat(saved.getId()).isEqualTo(secondCategory.getMenuCategoryInfo().getId());
			assertThat(saved.getOrder()).isEqualTo(patchOrder);
			assertThat(menuCategoryJpaRepository.findById(firstCategory.getMenuCategoryInfo().getId())
				.get().getOrder()).isEqualTo(secondCategory.getMenuCategoryInfo().getOrder());
		}
	}

	@Test
	void 카테고리_삭제_성공() {
		MenuCategoryEntity firstEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
		MenuCategoryEntity secondEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
		MenuCategory firstCategory = MenuCategoryMapper.toMenuCategory(firstEntity, savedStore);
		MenuCategory secondCategory = MenuCategoryMapper.toMenuCategory(secondEntity, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		menuCategoryRepository.deleteMenuCategory(savedStore.getId(),
			firstCategory.getMenuCategoryInfo().getId());

		assertThat(menuCategoryJpaRepository.findById(firstCategory.getMenuCategoryInfo().getId())).isEmpty();
		assertThat(menuCategoryJpaRepository.findById(secondCategory.getMenuCategoryInfo().getId())
			.get().getOrder()).isEqualTo(1);
	}
}
