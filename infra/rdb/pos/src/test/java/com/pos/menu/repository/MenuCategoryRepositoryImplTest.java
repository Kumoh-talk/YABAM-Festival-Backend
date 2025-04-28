package com.pos.menu.repository;

import static org.assertj.core.api.SoftAssertions.*;

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
import domain.pos.menu.repository.MenuCategoryRepository;
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
		// given
		MenuCategoryInfo menuCategoryInfo = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();

		// when
		MenuCategory menuCategory = menuCategoryRepository.postMenuCategory(savedStore, menuCategoryInfo);
		testEntityManager.flush();
		testEntityManager.clear();

		// then
		assertSoftly(softly -> {
			softly.assertThat(menuCategoryJpaRepository.findById(menuCategory.getMenuCategoryInfo().getId()))
				.isPresent();

			softly.assertThat(menuCategory.getStore()).isEqualTo(savedStore);
			softly.assertThat(menuCategory.getMenuCategoryInfo().getName()).isEqualTo(menuCategoryInfo.getName());
			softly.assertThat(menuCategory.getMenuCategoryInfo().getOrder()).isEqualTo(menuCategoryInfo.getOrder());
		});
	}

	@Test
	void 카테고리_정보_조회_성공() {
		// given
		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(),
				savedStore)));
		MenuCategory menuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);

		testEntityManager.flush();
		testEntityManager.clear();

		// when
		MenuCategoryInfo findMenuCategoryInfo = menuCategoryRepository.getMenuCategoryInfo(
			menuCategory.getStore().getStoreId(),
			menuCategory.getMenuCategoryInfo().getId()).get();

		// then
		assertSoftly(softly -> {
			softly.assertThat(findMenuCategoryInfo.getId()).isEqualTo(menuCategory.getMenuCategoryInfo().getId());
			softly.assertThat(findMenuCategoryInfo.getName()).isEqualTo(menuCategory.getMenuCategoryInfo().getName());
			softly.assertThat(findMenuCategoryInfo.getOrder()).isEqualTo(menuCategory.getMenuCategoryInfo().getOrder());
		});
	}

	@Test
	void 카테고리_정보_리스트_조회_성공() {
		// given
		MenuCategoryEntity menuCategoryEntityFirstOrder = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
		MenuCategoryEntity menuCategoryEntitySecondOrder = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
		MenuCategory menuCategoryFirstOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntityFirstOrder,
			savedStore);
		MenuCategory menuCategorySecondOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntitySecondOrder,
			savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		List<MenuCategoryInfo> findMenuCategoryInfo = menuCategoryRepository.getMenuCategoryInfoList(
			savedStore.getStoreId());

		// then
		assertSoftly(softly -> {
			softly.assertThat(findMenuCategoryInfo.size()).isEqualTo(2);
			softly.assertThat(findMenuCategoryInfo.get(0).getId())
				.isEqualTo(menuCategoryFirstOrder.getMenuCategoryInfo().getId());
			softly.assertThat(findMenuCategoryInfo.get(1).getId())
				.isEqualTo(menuCategorySecondOrder.getMenuCategoryInfo().getId());
		});
	}

	@Test
	void 카테고리_정보_수정_성공() {
		// given
		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(),
				savedStore)));
		MenuCategory menuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		MenuCategoryInfo patchMenuCategoryInfo = MenuCategoryInfoFixture.PATCH_GENERAL_MENU_INFO();

		// when
		MenuCategoryInfo savedMenuCategoryInfo = menuCategoryRepository.patchMenuCategory(patchMenuCategoryInfo);

		// then
		assertSoftly(softly -> {
			softly.assertThat(savedMenuCategoryInfo.getId()).isEqualTo(menuCategory.getMenuCategoryInfo().getId());
			softly.assertThat(savedMenuCategoryInfo.getOrder())
				.isEqualTo(menuCategory.getMenuCategoryInfo().getOrder());
			softly.assertThat(savedMenuCategoryInfo.getName()).isEqualTo(patchMenuCategoryInfo.getName());
		});
	}

	@Nested
	@DisplayName("메뉴 카테고리 순서 수정")
	class patchMenuOrder {
		private MenuCategory menuCategoryFirstOrder;
		private MenuCategory menuCategorySecondOrder;

		@BeforeEach
		void setUp() {
			MenuCategoryEntity menuCategoryEntityFirstOrder = testFixtureBuilder.buildMenuCategoryEntity(
				MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
					MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
			MenuCategoryEntity menuCategoryEntitySecondOrder = testFixtureBuilder.buildMenuCategoryEntity(
				MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
					MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
			menuCategoryFirstOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntityFirstOrder,
				savedStore);
			menuCategorySecondOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntitySecondOrder,
				savedStore);
			testEntityManager.flush();
			testEntityManager.clear();
		}

		@Test
		void 메뉴_순서_증가_성공() {
			// given
			Integer patchOrder = menuCategorySecondOrder.getMenuCategoryInfo().getOrder();

			// when
			MenuCategoryInfo savedMenuCategoryInfo = menuCategoryRepository.patchMenuCategoryOrder(
				savedStore.getStoreId(), menuCategoryFirstOrder.getMenuCategoryInfo(), patchOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(savedMenuCategoryInfo.getId())
					.isEqualTo(menuCategoryFirstOrder.getMenuCategoryInfo().getId());
				softly.assertThat(savedMenuCategoryInfo.getOrder()).isEqualTo(patchOrder);
				softly.assertThat(
						menuCategoryJpaRepository.findById(menuCategorySecondOrder.getMenuCategoryInfo().getId())
							.get()
							.getOrder())
					.isEqualTo(menuCategoryFirstOrder.getMenuCategoryInfo().getOrder());
			});
		}

		@Test
		void 메뉴_순서_감소_성공() {
			// given
			Integer patchOrder = menuCategoryFirstOrder.getMenuCategoryInfo().getOrder();

			// when
			MenuCategoryInfo savedMenuCategoryInfo = menuCategoryRepository.patchMenuCategoryOrder(
				savedStore.getStoreId(), menuCategorySecondOrder.getMenuCategoryInfo(), patchOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(savedMenuCategoryInfo.getId())
					.isEqualTo(menuCategorySecondOrder.getMenuCategoryInfo().getId());
				softly.assertThat(savedMenuCategoryInfo.getOrder()).isEqualTo(patchOrder);
				softly.assertThat(menuCategoryJpaRepository.findById(menuCategoryFirstOrder.getMenuCategoryInfo()
							.getId())
						.get()
						.getOrder())
					.isEqualTo(menuCategorySecondOrder.getMenuCategoryInfo().getOrder());
			});
		}
	}

	@Test
	void 메뉴_삭제_성공() {
		// given
		MenuCategoryEntity menuCategoryEntityFirstOrder = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.GENERAL_MENU_CATEGORY_INFO, savedStore)));
		MenuCategoryEntity menuCategoryEntitySecondOrder = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
				MenuCategoryFixture.DIFF_MENU_CATEGORY_INFO, savedStore)));
		MenuCategory menuCategoryFirstOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntityFirstOrder,
			savedStore);
		MenuCategory menuCategorySecondOrder = MenuCategoryMapper.toMenuCategory(menuCategoryEntitySecondOrder,
			savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		menuCategoryRepository.deleteMenuCategory(savedStore.getStoreId(),
			menuCategoryFirstOrder.getMenuCategoryInfo().getId());

		// then
		assertSoftly(softly -> {
			softly.assertThat(menuCategoryJpaRepository.findById(menuCategoryFirstOrder.getMenuCategoryInfo().getId()))
				.isEmpty();
			softly.assertThat(
					menuCategoryJpaRepository.findById(menuCategorySecondOrder.getMenuCategoryInfo().getId())
						.get()
						.getOrder())
				.isEqualTo(1);
		});
	}
}
