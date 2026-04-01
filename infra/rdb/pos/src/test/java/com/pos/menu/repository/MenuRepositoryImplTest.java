package com.pos.menu.repository;

import static fixtures.menu.MenuInfoFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import com.pos.fixtures.menu.MenuCategoryEntityFixture;
import com.pos.fixtures.menu.MenuEntityFixture;
import com.pos.fixtures.store.StoreEntityFixture;
import com.pos.global.config.RepositoryTest;
import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.entity.MenuEntity;
import com.pos.menu.mapper.MenuCategoryMapper;
import com.pos.menu.mapper.MenuMapper;
import com.pos.menu.repository.jpa.MenuJpaRepository;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.port.required.MenuRepository;
import domain.pos.store.entity.Store;
import fixtures.menu.MenuCategoryFixture;
import fixtures.menu.MenuCategoryInfoFixture;
import fixtures.menu.MenuFixture;
import fixtures.store.StoreFixture;

class MenuRepositoryImplTest extends RepositoryTest {
	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private MenuJpaRepository menuJpaRepository;

	private Store savedStore;
	private MenuCategory savedMenuCategory;

	@BeforeEach
	void setUp() {
		StoreEntity storeEntity = testFixtureBuilder.buildStoreEntity(
			StoreEntityFixture.CUSTOME_STORE_ENTITY(StoreFixture.GENERAL_CLOSE_STORE()));
		savedStore = StoreMapper.toStore(storeEntity);

		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(
				MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
					MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(), savedStore)));
		savedMenuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);

		testEntityManager.flush();
		testEntityManager.clear();
	}

	private MenuInfo firstMenuRequest() {
		return CUSTOM_MENU_INFO(null, GENERAL_MENU_ORDER, GENERAL_MENU_NAME,
			GENERAL_PRICE, GENERAL_DESCRIPTION, GENERAL_IMAGE_URL, false, false);
	}

	private MenuInfo secondMenuRequest() {
		return CUSTOM_MENU_INFO(null, DIFF_MENU_ORDER, DIFF_MENU_NAME,
			DIFF_PRICE, DIFF_DESCRIPTION, DIFF_IMAGE_URL, false, false);
	}

	@Test
	void 메뉴_추가_성공() {
		Menu menu = menuRepository.postMenu(savedStore, savedMenuCategory.getMenuCategoryInfo(),
			REQUEST_MENU_INFO());
		testEntityManager.flush();
		testEntityManager.clear();

		assertThat(menuJpaRepository.findById(menu.getMenuInfo().getId())).isPresent();
		assertThat(menu.getStore()).isEqualTo(savedStore);
		assertThat(menu.getMenuCategory().getMenuCategoryInfo())
			.isEqualTo(savedMenuCategory.getMenuCategoryInfo());
	}

	@Test
	void 메뉴_정보_조회_성공() {
		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
				savedMenuCategory.getMenuCategoryInfo()));
		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
		testEntityManager.flush();
		testEntityManager.clear();

		Optional<MenuInfo> result = menuRepository.getMenuInfo(savedStore.getId(), menuInfo.getId());

		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(menuInfo.getId());
		assertThat(result.get().getName()).isEqualTo(menuInfo.getName());
		assertThat(result.get().getOrder()).isEqualTo(menuInfo.getOrder());
	}

	@Test
	void 가게_전_메뉴_조회_및_락() {
		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
				savedMenuCategory.getMenuCategoryInfo()));
		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
		testEntityManager.flush();
		testEntityManager.clear();

		List<Menu> menus = menuRepository.getAllByStoreIdWithCategoryAndLock(savedStore.getId());

		assertThat(menus).hasSize(1);
		assertThat(menus.get(0).getMenuInfo().getId()).isEqualTo(menuInfo.getId());
	}

	@Nested
	@DisplayName("메뉴 슬라이스 조회")
	class getMenuSlice {
		private final int pageSize = 5;

		@Test
		void 첫_슬라이스_조회_성공() {
			MenuEntity firstEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			MenuEntity secondEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(secondMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			MenuInfo firstInfo = MenuMapper.toMenuInfo(firstEntity);
			MenuInfo secondInfo = MenuMapper.toMenuInfo(secondEntity);
			testEntityManager.flush();
			testEntityManager.clear();

			Slice<Menu> result = menuRepository.getMenuSlice(pageSize, savedStore.getId(), null, null);

			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent().get(0).getMenuInfo().getId()).isEqualTo(firstInfo.getId());
			assertThat(result.getContent().get(1).getMenuInfo().getId()).isEqualTo(secondInfo.getId());
		}

		@Test
		void 커서_슬라이스_조회_성공() {
			MenuEntity firstEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			MenuEntity secondEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(secondMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			MenuInfo firstInfo = MenuMapper.toMenuInfo(firstEntity);
			MenuInfo secondInfo = MenuMapper.toMenuInfo(secondEntity);
			testEntityManager.flush();
			testEntityManager.clear();

			Slice<Menu> result = menuRepository.getMenuSlice(pageSize, savedStore.getId(),
				firstInfo, savedMenuCategory.getMenuCategoryInfo());

			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getMenuInfo().getId()).isEqualTo(secondInfo.getId());
		}
	}

	@Test
	void 메뉴_정보_수정_성공() {
		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
				savedMenuCategory.getMenuCategoryInfo()));
		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
		testEntityManager.flush();
		testEntityManager.clear();

		MenuInfo patchInfo = PATCH_MENU_INFO(menuInfo);

		MenuInfo saved = menuRepository.patchMenuInfo(patchInfo);

		assertThat(saved.getId()).isEqualTo(menuInfo.getId());
		assertThat(saved.getName()).isEqualTo(patchInfo.getName());
		assertThat(saved.getPrice()).isEqualTo(patchInfo.getPrice());
		assertThat(saved.getDescription()).isEqualTo(patchInfo.getDescription());
		assertThat(saved.isSoldOut()).isEqualTo(patchInfo.isSoldOut());
	}

	@Nested
	@DisplayName("메뉴 순서 수정")
	class patchMenuOrder {
		private Menu menuFirstOrder;
		private Menu menuSecondOrder;

		@BeforeEach
		void setUp() {
			MenuEntity firstEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			MenuEntity secondEntity = testFixtureBuilder.buildMenuEntity(
				MenuEntityFixture.CUSTOM_MENU_ENTITY(secondMenuRequest(), savedStore,
					savedMenuCategory.getMenuCategoryInfo()));
			menuFirstOrder = MenuMapper.toMenu(firstEntity, savedStore, savedMenuCategory);
			menuSecondOrder = MenuMapper.toMenu(secondEntity, savedStore, savedMenuCategory);
			testEntityManager.flush();
			testEntityManager.clear();
		}

		@Test
		void 순서_증가_성공() {
			Integer patchOrder = menuSecondOrder.getMenuInfo().getOrder();

			MenuInfo saved = menuRepository.patchMenuOrder(menuFirstOrder, patchOrder);

			assertThat(saved.getId()).isEqualTo(menuFirstOrder.getMenuInfo().getId());
			assertThat(saved.getOrder()).isEqualTo(patchOrder);
			assertThat(menuJpaRepository.findById(menuSecondOrder.getMenuInfo().getId())
				.get().getOrder()).isEqualTo(menuFirstOrder.getMenuInfo().getOrder());
		}

		@Test
		void 순서_감소_성공() {
			Integer patchOrder = menuFirstOrder.getMenuInfo().getOrder();

			MenuInfo saved = menuRepository.patchMenuOrder(menuSecondOrder, patchOrder);

			assertThat(saved.getId()).isEqualTo(menuSecondOrder.getMenuInfo().getId());
			assertThat(saved.getOrder()).isEqualTo(patchOrder);
			assertThat(menuJpaRepository.findById(menuFirstOrder.getMenuInfo().getId())
				.get().getOrder()).isEqualTo(menuSecondOrder.getMenuInfo().getOrder());
		}
	}

	@Test
	void 메뉴_삭제_성공() {
		MenuEntity firstEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntityFixture.CUSTOM_MENU_ENTITY(firstMenuRequest(), savedStore,
				savedMenuCategory.getMenuCategoryInfo()));
		MenuEntity secondEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntityFixture.CUSTOM_MENU_ENTITY(secondMenuRequest(), savedStore,
				savedMenuCategory.getMenuCategoryInfo()));
		MenuInfo firstInfo = MenuMapper.toMenuInfo(firstEntity);
		MenuInfo secondInfo = MenuMapper.toMenuInfo(secondEntity);
		testEntityManager.flush();
		testEntityManager.clear();

		Menu menu = MenuFixture.CUSTOM_MENU(firstInfo, savedStore, savedMenuCategory);

		menuRepository.deleteMenu(menu);

		assertThat(menuJpaRepository.findById(menu.getMenuInfo().getId())).isEmpty();
		assertThat(menuJpaRepository.findById(secondInfo.getId()).get().getOrder()).isEqualTo(1);
	}
}
