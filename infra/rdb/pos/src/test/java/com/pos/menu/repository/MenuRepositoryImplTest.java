// package com.pos.menu.repository;
//
// import static org.assertj.core.api.SoftAssertions.*;
//
// import java.util.List;
// import java.util.Optional;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Slice;
//
// import com.pos.fixtures.menu.MenuCategoryEntityFixture;
// import com.pos.fixtures.menu.MenuEntityFixture;
// import com.pos.fixtures.store.StoreEntityFixture;
// import com.pos.global.config.RepositoryTest;
// import com.pos.menu.entity.MenuCategoryEntity;
// import com.pos.menu.entity.MenuEntity;
// import com.pos.menu.mapper.MenuCategoryMapper;
// import com.pos.menu.mapper.MenuMapper;
// import com.pos.menu.repository.jpa.MenuJpaRepository;
// import com.pos.store.entity.StoreEntity;
// import com.pos.store.mapper.StoreMapper;
//
// import domain.pos.menu.entity.Menu;
// import domain.pos.menu.entity.MenuCategory;
// import domain.pos.menu.entity.MenuInfo;
// import domain.pos.menu.repository.MenuRepository;
// import domain.pos.store.entity.Store;
// import fixtures.menu.MenuCategoryFixture;
// import fixtures.menu.MenuCategoryInfoFixture;
// import fixtures.menu.MenuFixture;
// import fixtures.menu.MenuInfoFixture;
// import fixtures.store.StoreFixture;
//
// class MenuRepositoryImplTest extends RepositoryTest {
// 	@Autowired
// 	private MenuRepository menuRepository;
//
// 	@Autowired
// 	private MenuJpaRepository menuJpaRepository;
//
// 	private Store savedStore;
// 	private MenuCategory savedMenuCategory;
//
// 	@BeforeEach
// 	void setUp() {
// 		StoreEntity storeEntity = testFixtureBuilder.buildStoreEntity(
// 			StoreEntityFixture.CUSTOME_STORE_ENTITY(StoreFixture.GENERAL_CLOSE_STORE()));
// 		savedStore = StoreMapper.toStore(storeEntity);
//
// 		MenuCategoryEntity menuCategoryEntity = testFixtureBuilder.buildMenuCategoryEntity(
// 			MenuCategoryEntityFixture.CUSTOM_MENU_CATEGORY_ENTITY(
// 				MenuCategoryFixture.CUSTOM_MENU_CATEGORY(
// 					MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO(), savedStore)));
// 		savedMenuCategory = MenuCategoryMapper.toMenuCategory(menuCategoryEntity, savedStore);
//
// 		testEntityManager.flush();
// 		testEntityManager.clear();
// 	}
//
// 	@Test
// 	void 메뉴_추가_성공() {
// 		// given
// 		MenuInfo menuInfo = MenuInfoFixture.REQUEST_MENU_INFO();
//
// 		// when
// 		Menu menu = menuRepository.postMenu(savedStore, savedMenuCategory.getMenuCategoryInfo(), menuInfo);
// 		testEntityManager.flush();
// 		testEntityManager.clear();
//
// 		// then
// 		assertSoftly(softly -> {
// 			softly.assertThat(menuJpaRepository.findById(menu.getMenuInfo().getId())).isPresent();
// 			softly.assertThat(menu.getStore()).isEqualTo(savedStore);
// 			softly.assertThat(menu.getMenuCategory().getMenuCategoryInfo())
// 				.isEqualTo(savedMenuCategory.getMenuCategoryInfo());
// 		});
// 	}
//
// 	@Test
// 	void 메뉴_정보_조회_성공() {
// 		// given
// 		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
// 			MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 				savedMenuCategory.getMenuCategoryInfo()));
// 		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
// 		testEntityManager.flush();
// 		testEntityManager.clear();
//
// 		// when
// 		Optional<MenuInfo> savedMenuInfoOptional = menuRepository.getMenuInfo(savedStore.getStoreId(),
// 			savedMenuCategory.getMenuCategoryInfo().getId());
//
// 		// then
// 		assertSoftly(softly -> {
// 			softly.assertThat(savedMenuInfoOptional).isPresent();
//
// 			MenuInfo savedMenuInfo = savedMenuInfoOptional.get();
// 			softly.assertThat(savedMenuInfo.getId()).isEqualTo(menuInfo.getId());
// 			softly.assertThat(savedMenuInfo.getOrder()).isEqualTo(menuInfo.getOrder());
// 			softly.assertThat(savedMenuInfo.getName()).isEqualTo(menuInfo.getName());
// 			softly.assertThat(savedMenuInfo.getPrice()).isEqualTo(menuInfo.getPrice());
// 			softly.assertThat(savedMenuInfo.getDescription()).isEqualTo(menuInfo.getDescription());
// 			softly.assertThat(savedMenuInfo.getImageUrl()).isEqualTo(menuInfo.getImageUrl());
// 			softly.assertThat(savedMenuInfo.isSoldOut()).isEqualTo(menuInfo.isSoldOut());
// 			softly.assertThat(savedMenuInfo.isRecommended()).isEqualTo(menuInfo.isRecommended());
// 		});
// 	}
//
// 	@Test
// 	void 가게_전_메뉴_조회_및_락() {
// 		// given
// 		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
// 			MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 				savedMenuCategory.getMenuCategoryInfo()));
// 		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
// 		testEntityManager.flush();
// 		testEntityManager.clear();
//
// 		// when
// 		List<Menu> menus = menuRepository.getAllByStoreIdWithCategoryAndLock(savedStore.getStoreId());
//
// 		// then
// 		assertSoftly(softly -> {
// 			softly.assertThat(menus).hasSize(1);
// 			Menu menu = menus.get(0);
// 			softly.assertThat(menu.getMenuInfo().getId()).isEqualTo(menuInfo.getId());
// 		});
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 슬라이스 조회")
// 	class getMenuSlice {
// 		private int pageSize = 5;
// 		private MenuInfo lastMenuInfo;
// 		private Long menuCategoryId;
//
// 		@BeforeEach
// 		void setUp() {
// 			menuCategoryId = savedMenuCategory.getMenuCategoryInfo().getId();
// 		}
//
// 		@Test
// 		void 메뉴_슬라이스_조회_성공() {
// 			// given
// 			testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			MenuEntity menuEntitySecondOrder = testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.DIFF_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			MenuInfo menuInfoSecondOrder = MenuMapper.toMenuInfo(menuEntitySecondOrder);
// 			testEntityManager.flush();
// 			testEntityManager.clear();
//
// 			lastMenuInfo = MenuInfoFixture.GENERAL_MENU_INFO();
//
// 			// when
// 			Slice<MenuInfo> menuInfos = menuRepository.getMenuSlice(pageSize, lastMenuInfo, menuCategoryId);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(menuInfos).hasSize(1);
// 				MenuInfo menuInfo = menuInfos.getContent().get(0);
// 				softly.assertThat(menuInfo.getId()).isEqualTo(menuInfoSecondOrder.getId());
// 			});
// 		}
//
// 		@Test
// 		void 첫_슬라이스_조회_성공() {
// 			// given
// 			MenuEntity menuEntityFirstOrder = testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			MenuEntity menuEntitySecondOrder = testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.DIFF_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			MenuInfo menuInfoFirstOrder = MenuMapper.toMenuInfo(menuEntityFirstOrder);
// 			MenuInfo menuInfoSecondOrder = MenuMapper.toMenuInfo(menuEntitySecondOrder);
// 			testEntityManager.flush();
// 			testEntityManager.clear();
//
// 			lastMenuInfo = null;
//
// 			// when
// 			Slice<MenuInfo> menuInfos = menuRepository.getMenuSlice(pageSize, lastMenuInfo, menuCategoryId);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(menuInfos).hasSize(2);
// 				softly.assertThat(menuInfos.getContent().get(0).getId()).isEqualTo(menuInfoFirstOrder.getId());
// 				softly.assertThat(menuInfos.getContent().get(1).getId()).isEqualTo(menuInfoSecondOrder.getId());
// 			});
// 		}
// 	}
//
// 	@Test
// 	void 메뉴_정보_수정_성공() {
// 		// given
// 		MenuEntity menuEntity = testFixtureBuilder.buildMenuEntity(
// 			MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 				savedMenuCategory.getMenuCategoryInfo()));
// 		MenuInfo menuInfo = MenuMapper.toMenuInfo(menuEntity);
// 		testEntityManager.flush();
// 		testEntityManager.clear();
//
// 		MenuInfo patchMenuInfo = MenuInfoFixture.PATCH_MENU_INFO(menuInfo);
//
// 		// when
// 		MenuInfo savedMenuInfo = menuRepository.patchMenuInfo(patchMenuInfo);
//
// 		// then
// 		assertSoftly(softly -> {
// 			softly.assertThat(savedMenuInfo.getId()).isEqualTo(menuInfo.getId());
// 			softly.assertThat(savedMenuInfo.getOrder()).isEqualTo(menuInfo.getOrder());
// 			softly.assertThat(savedMenuInfo.getName()).isEqualTo(patchMenuInfo.getName());
// 			softly.assertThat(savedMenuInfo.getPrice()).isEqualTo(patchMenuInfo.getPrice());
// 			softly.assertThat(savedMenuInfo.getDescription()).isEqualTo(patchMenuInfo.getDescription());
// 			softly.assertThat(savedMenuInfo.getImageUrl()).isEqualTo(patchMenuInfo.getImageUrl());
// 			softly.assertThat(savedMenuInfo.isSoldOut()).isEqualTo(patchMenuInfo.isSoldOut());
// 			softly.assertThat(savedMenuInfo.isRecommended()).isEqualTo(patchMenuInfo.isRecommended());
// 		});
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 순서 수정")
// 	class patchMenuOrder {
// 		private Menu menuFirstOrder;
// 		private Menu menuSecondOrder;
//
// 		@BeforeEach
// 		void setUp() {
// 			MenuEntity menuEntityFirstOrder = testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			MenuEntity menuEntitySecondOrder = testFixtureBuilder.buildMenuEntity(
// 				MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.DIFF_MENU_INFO(), savedStore,
// 					savedMenuCategory.getMenuCategoryInfo()));
// 			menuFirstOrder = MenuMapper.toMenu(menuEntityFirstOrder, savedStore, savedMenuCategory);
// 			menuSecondOrder = MenuMapper.toMenu(menuEntitySecondOrder, savedStore, savedMenuCategory);
// 			testEntityManager.flush();
// 			testEntityManager.clear();
// 		}
//
// 		@Test
// 		void 메뉴_순서_증가_성공() {
// 			// given
// 			Integer patchOrder = menuSecondOrder.getMenuInfo().getOrder();
//
// 			// when
// 			MenuInfo savedMenuInfo = menuRepository.patchMenuOrder(menuFirstOrder, patchOrder);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(savedMenuInfo.getId()).isEqualTo(menuFirstOrder.getMenuInfo().getId());
// 				softly.assertThat(savedMenuInfo.getOrder()).isEqualTo(patchOrder);
// 				softly.assertThat(menuJpaRepository.findById(menuSecondOrder.getMenuInfo().getId()).get().getOrder())
// 					.isEqualTo(menuFirstOrder.getMenuInfo().getOrder());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_순서_감소_성공() {
// 			// given
// 			Integer patchOrder = menuFirstOrder.getMenuInfo().getOrder();
//
// 			// when
// 			MenuInfo savedMenuInfo = menuRepository.patchMenuOrder(menuSecondOrder, patchOrder);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(savedMenuInfo.getId()).isEqualTo(menuSecondOrder.getMenuInfo().getId());
// 				softly.assertThat(savedMenuInfo.getOrder()).isEqualTo(patchOrder);
// 				softly.assertThat(menuJpaRepository.findById(menuFirstOrder.getMenuInfo().getId()).get().getOrder())
// 					.isEqualTo(menuSecondOrder.getMenuInfo().getOrder());
// 			});
// 		}
// 	}
//
// 	@Test
// 	void 메뉴_삭제_성공() {
// 		// given
// 		MenuEntity menuEntityFirstOrder = testFixtureBuilder.buildMenuEntity(
// 			MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.GENERAL_MENU_INFO(), savedStore,
// 				savedMenuCategory.getMenuCategoryInfo()));
// 		MenuEntity menuEntitySecondOrder = testFixtureBuilder.buildMenuEntity(
// 			MenuEntityFixture.CUSTOM_MENU_ENTITY(MenuInfoFixture.DIFF_MENU_INFO(), savedStore,
// 				savedMenuCategory.getMenuCategoryInfo()));
// 		MenuInfo menuInfoFirstOrder = MenuMapper.toMenuInfo(menuEntityFirstOrder);
// 		MenuInfo menuInfoSecondOrder = MenuMapper.toMenuInfo(menuEntitySecondOrder);
// 		testEntityManager.flush();
// 		testEntityManager.clear();
//
// 		Menu menu = MenuFixture.CUSTOM_MENU(menuInfoFirstOrder, savedStore, savedMenuCategory);
//
// 		// when
// 		menuRepository.deleteMenu(menu);
//
// 		// then
// 		assertSoftly(softly -> {
// 			softly.assertThat(menuJpaRepository.findById(menu.getMenuInfo().getId())).isEmpty();
// 			softly.assertThat(menuJpaRepository.findById(menuInfoSecondOrder.getId()).get().getOrder())
// 				.isEqualTo(1);
// 		});
// 	}
// }
