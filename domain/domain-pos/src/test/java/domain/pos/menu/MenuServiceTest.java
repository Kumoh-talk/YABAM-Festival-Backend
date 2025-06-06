// package domain.pos.menu;
//
// import static fixtures.member.UserFixture.*;
// import static fixtures.menu.MenuCategoryFixture.GENERAL_MENU_CATEGORY;
// import static fixtures.menu.MenuCategoryFixture.*;
// import static fixtures.menu.MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO;
// import static fixtures.menu.MenuCategoryInfoFixture.*;
// import static fixtures.menu.MenuFixture.*;
// import static fixtures.menu.MenuInfoFixture.GENERAL_MENU_INFO;
// import static fixtures.menu.MenuInfoFixture.*;
// import static fixtures.store.StoreFixture.*;
// import static fixtures.store.StoreInfoFixture.*;
// import static org.assertj.core.api.SoftAssertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.mockito.BDDMockito;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Slice;
// import org.springframework.data.domain.SliceImpl;
//
// import com.exception.ErrorCode;
// import com.exception.ServiceException;
// import com.vo.UserPassport;
//
// import base.ServiceTest;
// import domain.pos.menu.entity.Menu;
// import domain.pos.menu.entity.MenuCategory;
// import domain.pos.menu.entity.MenuCategoryInfo;
// import domain.pos.menu.entity.MenuInfo;
// import domain.pos.menu.implement.MenuCategoryReader;
// import domain.pos.menu.implement.MenuCategoryValidator;
// import domain.pos.menu.implement.MenuReader;
// import domain.pos.menu.implement.MenuValidator;
// import domain.pos.menu.implement.MenuWriter;
// import domain.pos.menu.service.MenuService;
// import domain.pos.store.entity.Store;
// import domain.pos.store.implement.StoreReader;
// import domain.pos.store.implement.StoreValidator;
//
// public class MenuServiceTest extends ServiceTest {
// 	@Mock
// 	private StoreValidator storeValidator;
// 	@Mock
// 	private MenuValidator menuValidator;
// 	@Mock
// 	private MenuCategoryValidator menuCategoryValidator;
// 	@Mock
// 	private StoreReader storeReader;
// 	@Mock
// 	private MenuCategoryReader menuCategoryReader;
// 	@Mock
// 	private MenuReader menuReader;
// 	@Mock
// 	private MenuWriter menuWriter;
// 	@InjectMocks
// 	private MenuService menuService;
//
// 	@Nested
// 	@DisplayName("메뉴 생성")
// 	class postMenu {
// 		private final Long storeId = 1L;
// 		private final UserPassport userPassport = OWNER_USER_PASSPORT();
// 		private final Long menuCategoryId = GENERAL_MENU_CATEGORY_ID;
// 		private final MenuInfo requestMenuInfo = REQUEST_MENU_INFO();
//
// 		private final Long menuId = 3L;
//
// 		@Test
// 		void 메뉴_생성_성공() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();
// 			MenuCategory menuCategory = CUSTOM_MENU_CATEGORY(menuCategoryInfo, store);
// 			Menu menu = CUSTOM_MENU(REQUEST_TO_ENTITY(menuId, requestMenuInfo), menuCategory.getStore(), menuCategory);
//
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuCategoryReader.getMenuCategoryInfo(storeId, menuCategoryId))
// 				.willReturn(Optional.of(menuCategoryInfo));
// 			BDDMockito.given(menuWriter.postMenu(store, menuCategoryInfo, requestMenuInfo))
// 				.willReturn(menu);
//
// 			// when
// 			Menu serviceMenu = menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);
//
// 			// then
// 			assertSoftly(softly -> {
// 				MenuInfo serviceMenuInfo = serviceMenu.getMenuInfo();
// 				MenuCategory serviceMenuCategory = serviceMenu.getMenuCategory();
//
// 				softly.assertThat(serviceMenuInfo.getId()).isEqualTo(menuId);
// 				softly.assertThat(serviceMenuInfo.getName()).isEqualTo(requestMenuInfo.getName());
// 				softly.assertThat(serviceMenuInfo.getPrice()).isEqualTo(requestMenuInfo.getPrice());
// 				softly.assertThat(serviceMenuInfo.getDescription()).isEqualTo(requestMenuInfo.getDescription());
// 				softly.assertThat(serviceMenuInfo.getImageUrl()).isEqualTo(requestMenuInfo.getImageUrl());
//
// 				softly.assertThat(serviceMenuCategory.getMenuCategoryInfo().getId())
// 					.isEqualTo(menuCategoryInfo.getId());
//
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuCategoryReader)
// 					.getMenuCategoryInfo(storeId, menuCategoryId);
// 				verify(menuValidator)
// 					.validateMenuOrder(menuCategoryId, requestMenuInfo);
// 				verify(menuWriter)
// 					.postMenu(store, menuCategoryInfo, requestMenuInfo);
// 			});
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
//
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuCategoryReader, never())
// 					.getMenuCategoryInfo(anyLong(), anyLong());
// 				verify(menuValidator, never())
// 					.validateMenuOrder(anyLong(), any());
// 				verify(menuWriter, never())
// 					.postMenu(any(), any(), any());
// 			});
// 		}
//
// 		@Test
// 		void 점주_인증_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuCategoryReader, never())
// 					.getMenuCategoryInfo(anyLong(), anyLong());
// 				verify(menuValidator, never())
// 					.validateMenuOrder(anyLong(), any());
// 				verify(menuWriter, never())
// 					.postMenu(any(), any(), any());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_카테고리_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuCategoryReader.getMenuCategoryInfo(storeId, menuCategoryId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuCategoryReader)
// 					.getMenuCategoryInfo(storeId, menuCategoryId);
// 				verify(menuValidator, never())
// 					.validateMenuOrder(anyLong(), any());
// 				verify(menuWriter, never())
// 					.postMenu(any(), any(), any());
// 			});
// 		}
//
// 		@Test
// 		void 중복된_메뉴_순서_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuCategoryReader.getMenuCategoryInfo(storeId, menuCategoryId))
// 				.willReturn(Optional.of(menuCategoryInfo));
// 			doThrow(new ServiceException(ErrorCode.EXIST_MENU_ORDER))
// 				.when(menuValidator)
// 				.validateMenuOrder(menuCategoryId, requestMenuInfo);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_MENU_ORDER);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuCategoryReader)
// 					.getMenuCategoryInfo(storeId, menuCategoryId);
// 				verify(menuValidator)
// 					.validateMenuOrder(menuCategoryId, requestMenuInfo);
// 				verify(menuWriter, never())
// 					.postMenu(any(), any(), any());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 단일 조회")
// 	class getMenuInfo {
// 		private final Long storeId = 1L;
// 		private final Long menuId = GENERAL_MENU_ID;
//
// 		@Test
// 		void 메뉴_단일_조회_성공() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			MenuInfo menuInfo = GENERAL_MENU_INFO();
//
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.of(store));
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, menuId))
// 				.willReturn(Optional.of(menuInfo));
//
// 			// when
// 			MenuInfo serviceMenuInfo = menuService.getMenuInfo(storeId, menuId);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(serviceMenuInfo.getId()).isEqualTo(menuId);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, menuId);
// 			});
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuReader, never())
// 					.getMenuInfo(storeId, menuId);
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.of(store));
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, menuId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, menuId);
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 리스트 조회")
// 	class getMenuSlice {
// 		private final int pageSize = 10;
// 		private final boolean hasNext = false;
// 		private final Pageable pageable = Pageable.ofSize(pageSize);
// 		private final Long lastMenuId = GENERAL_MENU_ID;
// 		private final Long storeId = 1L;
// 		private final Long menuCategoryId = 1L;
//
// 		@Test
// 		void 메뉴_리스트_조회_성공() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			MenuInfo lastMenuInfo = GENERAL_MENU_INFO();
// 			MenuInfo nextMenuInfo = CUSTOM_MENU_INFO(lastMenuId + 1, GENERAL_MENU_ORDER + 1,
// 				GENERAL_MENU_NAME, GENERAL_PRICE, GENERAL_DESCRIPTION, GENERAL_IMAGE_URL, GENERAL_IS_SOLD_OUT,
// 				GENERAL_IS_RECOMMENDED);
// 			Slice<MenuInfo> menuSlice = new SliceImpl<>(new ArrayList<>(List.of(nextMenuInfo)), pageable, hasNext);
//
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.of(store));
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, lastMenuId))
// 				.willReturn(Optional.of(lastMenuInfo));
// 			BDDMockito.given(menuReader.getMenuSlice(pageSize, lastMenuInfo, menuCategoryId))
// 				.willReturn(menuSlice);
//
// 			// when
// 			Slice<MenuInfo> serviceMenuSlice = menuService.getMenuSlice(pageSize, lastMenuId, storeId, menuCategoryId);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(serviceMenuSlice.getSize()).isEqualTo(pageSize);
// 				softly.assertThat(serviceMenuSlice.hasNext()).isEqualTo(hasNext);
// 				softly.assertThat(serviceMenuSlice.getContent().get(0).getOrder())
// 					.isEqualTo(GENERAL_MENU_ORDER + 1);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuCategoryValidator)
// 					.validateMenuCategory(storeId, menuCategoryId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, lastMenuId);
// 				verify(menuReader)
// 					.getMenuSlice(pageSize, lastMenuInfo, menuCategoryId);
// 			});
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.getMenuSlice(pageSize, lastMenuId, storeId, menuCategoryId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuCategoryValidator, never())
// 					.validateMenuCategory(anyLong(), anyLong());
// 				verify(menuReader, never())
// 					.getMenuInfo(anyLong(), anyLong());
// 				verify(menuReader, never())
// 					.getMenuSlice(anyInt(), any(), anyLong());
// 			});
// 		}
//
// 		@Test
// 		void 카테고리_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeReader.readSingleStore(any(Long.class)))
// 				.willReturn(Optional.of(store));
//
// 			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
// 				.when(menuCategoryValidator)
// 				.validateMenuCategory(storeId, menuCategoryId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.getMenuSlice(pageSize, lastMenuId, storeId, menuCategoryId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuCategoryValidator)
// 					.validateMenuCategory(storeId, menuCategoryId);
// 				verify(menuReader, never())
// 					.getMenuInfo(anyLong(), anyLong());
// 				verify(menuReader, never())
// 					.getMenuSlice(anyInt(), any(), anyLong());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeReader.readSingleStore(storeId))
// 				.willReturn(Optional.of(store));
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, lastMenuId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.getMenuSlice(pageSize, lastMenuId, storeId, menuCategoryId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
// 				verify(storeReader)
// 					.readSingleStore(storeId);
// 				verify(menuCategoryValidator)
// 					.validateMenuCategory(storeId, menuCategoryId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, lastMenuId);
// 				verify(menuReader, never())
// 					.getMenuSlice(anyInt(), any(), anyLong());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 수정")
// 	class patchMenu {
// 		private final Long storeId = 1L;
// 		private final UserPassport userPassport = OWNER_USER_PASSPORT();
// 		private final MenuInfo patchMenuInfo = GENERAL_MENU_INFO();
//
// 		@Test
// 		void 메뉴_수정_성공() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, patchMenuInfo.getId()))
// 				.willReturn(Optional.of(patchMenuInfo));
// 			BDDMockito.given(menuWriter.patchMenuInfo(patchMenuInfo))
// 				.willReturn(patchMenuInfo);
//
// 			// when
// 			MenuInfo servicePatchMenuInfo = menuService.patchMenuInfo(storeId, userPassport, patchMenuInfo);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(servicePatchMenuInfo.getId()).isEqualTo(patchMenuInfo.getId());
// 				softly.assertThat(servicePatchMenuInfo.getName()).isEqualTo(patchMenuInfo.getName());
// 				softly.assertThat(servicePatchMenuInfo.getPrice()).isEqualTo(patchMenuInfo.getPrice());
// 				softly.assertThat(servicePatchMenuInfo.getDescription()).isEqualTo(patchMenuInfo.getDescription());
// 				softly.assertThat(servicePatchMenuInfo.getImageUrl()).isEqualTo(patchMenuInfo.getImageUrl());
// 				softly.assertThat(servicePatchMenuInfo.isSoldOut()).isEqualTo(patchMenuInfo.isSoldOut());
//
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, patchMenuInfo.getId());
// 				verify(menuWriter)
// 					.patchMenuInfo(patchMenuInfo);
// 			});
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuInfo(storeId, userPassport, patchMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuInfo(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.patchMenuInfo(any());
// 			});
// 		}
//
// 		@Test
// 		void 점주_인증_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuInfo(storeId, userPassport, patchMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuInfo(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.patchMenuInfo(any());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuInfo(storeId, patchMenuInfo.getId()))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuInfo(storeId, userPassport, patchMenuInfo))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader)
// 					.getMenuInfo(storeId, patchMenuInfo.getId());
// 				verify(menuWriter, never())
// 					.patchMenuInfo(any());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 순서 수정")
// 	class patchMenuOrder {
// 		private final Long storeId = 1L;
// 		private final UserPassport userPassport = OWNER_USER_PASSPORT();
// 		private final Long menuId = 2L;
// 		private final int patchOrder = 2;
//
// 		@Test
// 		void 메뉴_순서_수정_성공() {
// 			// given
// 			MenuInfo patchMenuInfo = CUSTOM_MENU_INFO(menuId, patchOrder, GENERAL_MENU_NAME,
// 				GENERAL_PRICE, GENERAL_DESCRIPTION, GENERAL_IMAGE_URL, GENERAL_IS_SOLD_OUT, GENERAL_IS_RECOMMENDED);
// 			MenuCategory menuCategory = GENERAL_MENU_CATEGORY();
// 			Menu menu = CUSTOM_MENU(patchMenuInfo, null, menuCategory);
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
//
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId))
// 				.willReturn(Optional.of(menu));
// 			BDDMockito.given(menuWriter.patchMenuOrder(menu, patchOrder))
// 				.willReturn(patchMenuInfo);
//
// 			// when
// 			MenuInfo servicePatchMenuInfo = menuService.patchMenuOrder(storeId, userPassport, menuId, patchOrder);
//
// 			// then
// 			assertSoftly(softly -> {
// 				softly.assertThat(servicePatchMenuInfo.getId()).isEqualTo(menuId);
// 				softly.assertThat(servicePatchMenuInfo.getOrder()).isEqualTo(patchOrder);
// 				softly.assertThat(servicePatchMenuInfo.getName()).isEqualTo(patchMenuInfo.getName());
// 				softly.assertThat(servicePatchMenuInfo.getPrice()).isEqualTo(patchMenuInfo.getPrice());
// 				softly.assertThat(servicePatchMenuInfo.getDescription()).isEqualTo(patchMenuInfo.getDescription());
// 				softly.assertThat(servicePatchMenuInfo.getImageUrl()).isEqualTo(patchMenuInfo.getImageUrl());
// 				softly.assertThat(servicePatchMenuInfo.isSoldOut()).isEqualTo(patchMenuInfo.isSoldOut());
// 				softly.assertThat(servicePatchMenuInfo.isRecommended()).isEqualTo(patchMenuInfo.isRecommended());
//
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader)
// 					.getMenuWithCategoryAndStoreLock(storeId, menuId);
// 				verify(menuWriter)
// 					.patchMenuOrder(menu, patchOrder);
// 			});
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuOrder(storeId, userPassport, menuId, patchOrder))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuWithCategoryAndStoreLock(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.patchMenuOrder(any(), anyInt());
// 			});
// 		}
//
// 		@Test
// 		void 점주_인증_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuOrder(storeId, userPassport, menuId, patchOrder))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuWithCategoryAndStoreLock(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.patchMenuOrder(any(), anyInt());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.patchMenuOrder(storeId, userPassport, menuId, patchOrder))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader)
// 					.getMenuWithCategoryAndStoreLock(storeId, menuId);
// 				verify(menuWriter, never())
// 					.patchMenuOrder(any(), anyInt());
// 			});
// 		}
// 	}
//
// 	@Nested
// 	@DisplayName("메뉴 삭제")
// 	class deleteMenu {
// 		private final Long storeId = 1L;
// 		private final UserPassport userPassport = OWNER_USER_PASSPORT();
// 		private final Long menuId = 4L;
//
// 		@Test
// 		void 메뉴_삭제_성공() {
// 			// given
// 			Menu menu = GENERAL_MENU();
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId))
// 				.willReturn(Optional.of(menu));
//
// 			// when
// 			menuService.deleteMenu(storeId, userPassport, menuId);
//
// 			// then
// 			verify(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
// 			verify(menuReader)
// 				.getMenuWithCategoryAndStoreLock(storeId, menuId);
// 			verify(menuWriter)
// 				.deleteMenu(menu);
// 		}
//
// 		@Test
// 		void 주점_조회_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.deleteMenu(storeId, userPassport, menuId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuWithCategoryAndStoreLock(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.deleteMenu(any());
// 			});
// 		}
//
// 		@Test
// 		void 점주_인증_실패() {
// 			// given
// 			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
// 				.when(storeValidator)
// 				.validateStoreOwner(userPassport, storeId);
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.deleteMenu(storeId, userPassport, menuId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader, never())
// 					.getMenuWithCategoryAndStoreLock(anyLong(), anyLong());
// 				verify(menuWriter, never())
// 					.deleteMenu(any());
// 			});
// 		}
//
// 		@Test
// 		void 메뉴_조회_실패() {
// 			// given
// 			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
// 			BDDMockito.given(storeValidator.validateStoreOwner(userPassport, storeId))
// 				.willReturn(store);
// 			BDDMockito.given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId))
// 				.willReturn(Optional.empty());
//
// 			// when -> then
// 			assertSoftly(softly -> {
// 				softly.assertThatThrownBy(
// 						() -> menuService.deleteMenu(storeId, userPassport, menuId))
// 					.isInstanceOf(ServiceException.class)
// 					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
// 				verify(storeValidator)
// 					.validateStoreOwner(userPassport, storeId);
// 				verify(menuReader)
// 					.getMenuWithCategoryAndStoreLock(storeId, menuId);
// 				verify(menuWriter, never())
// 					.deleteMenu(any());
// 			});
// 		}
// 	}
// }
