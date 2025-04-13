package domain.pos.menu;

import static fixtures.member.UserFixture.*;
import static fixtures.menu.MenuCategoryFixture.*;
import static fixtures.menu.MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO;
import static fixtures.menu.MenuCategoryInfoFixture.*;
import static fixtures.menu.MenuFixture.*;
import static fixtures.menu.MenuInfoFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.store.StoreInfoFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuReader;
import domain.pos.menu.implement.MenuValidator;
import domain.pos.menu.implement.MenuWriter;
import domain.pos.menu.service.MenuService;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;

public class MenuServiceTest extends ServiceTest {
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private MenuValidator menuValidator;
	@Mock
	private MenuCategoryValidator menuCategoryValidator;
	@Mock
	private StoreReader storeReader;
	@Mock
	private MenuReader menuReader;
	@Mock
	private MenuWriter menuWriter;
	@InjectMocks
	private MenuService menuService;

	@Nested
	@DisplayName("메뉴 생성")
	class postMenu {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final Long menuCategoryId = GENERAL_MENU_CATEGORY_ID;
		private final MenuInfo requestMenuInfo = REQUEST_MENU_INFO();

		private final Long menuId = 3L;

		@Test
		void 메뉴_생성_성공() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();
			MenuCategory menuCategory = CUSTOM_MENU_CATEGORY(menuCategoryInfo, store);
			Menu menu = CUSTOM_MENU(REQUEST_TO_ENTITY(menuId, requestMenuInfo), menuCategory.getStore(), menuCategory);

			BDDMockito.given(menuWriter.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
				.willReturn(menu);

			// when
			Menu serviceMenu = menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);

			// then
			assertSoftly(softly -> {
				MenuInfo serviceMenuInfo = serviceMenu.getMenuInfo();
				MenuCategory serviceMenuCategory = serviceMenu.getMenuCategory();

				softly.assertThat(serviceMenuInfo.getMenuId()).isEqualTo(menuId);
				softly.assertThat(serviceMenuInfo.getMenuName()).isEqualTo(requestMenuInfo.getMenuName());
				softly.assertThat(serviceMenuInfo.getPrice()).isEqualTo(requestMenuInfo.getPrice());
				softly.assertThat(serviceMenuInfo.getDescription()).isEqualTo(requestMenuInfo.getDescription());
				softly.assertThat(serviceMenuInfo.getImageUrl()).isEqualTo(requestMenuInfo.getImageUrl());

				softly.assertThat(serviceMenuCategory.getMenuCategoryInfo().getMenuCategoryId())
					.isEqualTo(menuCategoryInfo.getMenuCategoryId());
			});
		}

		@Test
		void 주점_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryId);
				verify(menuValidator, never())
					.validateMenuOrder(menuCategoryId, requestMenuInfo);
				verify(menuWriter, never())
					.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);
			});
		}

		@Test
		void 점주_인증_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryId);
				verify(menuValidator, never())
					.validateMenuOrder(menuCategoryId, requestMenuInfo);
				verify(menuWriter, never())
					.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);
			});
		}

		@Test
		void 메뉴_카테고리_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator)
				.validateMenuCategory(menuCategoryId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryId);
				verify(menuValidator, never())
					.validateMenuOrder(menuCategoryId, requestMenuInfo);
				verify(menuWriter, never())
					.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);
			});
		}

		@Test
		void 중복된_메뉴_순서_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.EXIST_MENU_ORDER))
				.when(menuValidator)
				.validateMenuOrder(menuCategoryId, requestMenuInfo);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_MENU_ORDER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryId);
				verify(menuValidator)
					.validateMenuOrder(menuCategoryId, requestMenuInfo);
				verify(menuWriter, never())
					.postMenu(storeId, userPassport, menuCategoryId, requestMenuInfo);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 단일 조회")
	class getMenuInfo {
		private final Long storeId = 1L;
		private final Long menuId = GENERAL_MENU_ID;

		@Test
		void 메뉴_단일_조회_성공() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			MenuInfo menuInfo = GENERAL_MENU_INFO();

			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.of(store));
			BDDMockito.given(menuReader.getMenuInfo(menuId))
				.willReturn(Optional.of(menuInfo));

			// when
			MenuInfo serviceMenuInfo = menuService.getMenuInfo(storeId, menuId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(serviceMenuInfo.getMenuId()).isEqualTo(menuId);
			});
		}

		@Test
		void 주점_조회_실패() {
			// given
			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuReader, never())
					.getMenuInfo(menuId);
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			BDDMockito.given(storeReader.readSingleStore(any(Long.class)))
				.willReturn(Optional.of(store));
			BDDMockito.given(menuReader.getMenuInfo(any(Long.class)))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuReader)
					.getMenuInfo(menuId);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 리스트 조회")
	class getMenuSlice {
		private final int size = 10;
		private final boolean hasNext = false;
		private final Pageable pageable = Pageable.ofSize(size);
		private final Long lastMenuId = GENERAL_MENU_ID;
		private final Long storeId = 1L;
		private final Long menuCategoryId = 1L;

		@Test
		void 메뉴_리스트_조회_성공() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			MenuInfo lastMenuInfo = GENERAL_MENU_INFO();
			MenuInfo nextMenuInfo = CUSTOM_MENU_INFO(lastMenuId + 1, GENERAL_MENU_ORDER + 1,
				GENERAL_MENU_NAME, GENERAL_PRICE, GENERAL_DESCRIPTION, GENERAL_IMAGE_URL, GENERAL_IS_SOLD_OUT,
				GENERAL_IS_RECOMMENDED);
			Slice<MenuInfo> menuSlice = new SliceImpl<>(new ArrayList<>(List.of(nextMenuInfo)), pageable, hasNext);

			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.of(store));
			BDDMockito.given(menuReader.getMenuInfo(lastMenuId))
				.willReturn(Optional.of(lastMenuInfo));
			BDDMockito.given(menuReader.getMenuSlice(pageable, lastMenuInfo, menuCategoryId))
				.willReturn(menuSlice);

			// when
			Slice<MenuInfo> serviceMenuSlice = menuService.getMenuSlice(pageable, lastMenuId, storeId, menuCategoryId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(serviceMenuSlice.getSize()).isEqualTo(size);
				softly.assertThat(serviceMenuSlice.hasNext()).isEqualTo(hasNext);
				softly.assertThat(serviceMenuSlice.getContent().get(0).getMenuOrder())
					.isEqualTo(GENERAL_MENU_ORDER + 1);
			});
		}

		@Test
		void 주점_조회_실패() {
			// given
			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.getMenuSlice(pageable, lastMenuId, storeId, menuCategoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryId);
				verify(menuReader, never())
					.getMenuInfo(lastMenuId);
				verify(menuReader, never())
					.getMenuSlice(any(Pageable.class), any(MenuInfo.class), any(Long.class));
			});
		}

		@Test
		void 카테고리_조회_실패() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			BDDMockito.given(storeReader.readSingleStore(any(Long.class)))
				.willReturn(Optional.of(store));

			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator)
				.validateMenuCategory(menuCategoryId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.getMenuSlice(pageable, lastMenuId, storeId, menuCategoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryId);
				verify(menuReader, never())
					.getMenuInfo(lastMenuId);
				verify(menuReader, never())
					.getMenuSlice(any(Pageable.class), any(MenuInfo.class), any(Long.class));
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			Store store = CUSTOM_STORE(storeId, GENERAL_STORE_INFO(), OWNER_USER_PASSPORT());
			BDDMockito.given(storeReader.readSingleStore(any(Long.class)))
				.willReturn(Optional.of(store));
			BDDMockito.given(menuReader.getMenuInfo(any(Long.class)))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.getMenuSlice(pageable, lastMenuId, storeId, menuCategoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryId);
				verify(menuReader)
					.getMenuInfo(lastMenuId);
				verify(menuReader, never())
					.getMenuSlice(any(Pageable.class), any(MenuInfo.class), any(Long.class));
			});
		}
	}

	@Nested
	@DisplayName("메뉴 수정")
	class patchMenu {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final MenuInfo patchMenuInfo = GENERAL_MENU_INFO();

		@Test
		void 메뉴_수정_성공() {
			// given
			BDDMockito.given(menuWriter.patchMenu(patchMenuInfo))
				.willReturn(patchMenuInfo);

			// when
			MenuInfo servicePatchMenuInfo = menuService.patchMenu(storeId, userPassport, patchMenuInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(servicePatchMenuInfo.getMenuId()).isEqualTo(patchMenuInfo.getMenuId());
				softly.assertThat(servicePatchMenuInfo.getMenuName()).isEqualTo(patchMenuInfo.getMenuName());
				softly.assertThat(servicePatchMenuInfo.getPrice()).isEqualTo(patchMenuInfo.getPrice());
				softly.assertThat(servicePatchMenuInfo.getDescription()).isEqualTo(patchMenuInfo.getDescription());
				softly.assertThat(servicePatchMenuInfo.getImageUrl()).isEqualTo(patchMenuInfo.getImageUrl());
				softly.assertThat(servicePatchMenuInfo.isSoldOut()).isEqualTo(patchMenuInfo.isSoldOut());
			});
		}

		@Test
		void 주점_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenu(storeId, userPassport, patchMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(patchMenuInfo.getMenuId());
				verify(menuWriter, never())
					.patchMenu(patchMenuInfo);
			});
		}

		@Test
		void 점주_인증_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenu(storeId, userPassport, patchMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(patchMenuInfo.getMenuId());
				verify(menuWriter, never())
					.patchMenu(patchMenuInfo);
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_NOT_FOUND))
				.when(menuValidator)
				.validateMenu(patchMenuInfo.getMenuId());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenu(storeId, userPassport, patchMenuInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator)
					.validateMenu(patchMenuInfo.getMenuId());
				verify(menuWriter, never())
					.patchMenu(patchMenuInfo);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 순서 수정")
	class patchMenuOrder {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final Long menuCategoryId = 1L;
		private final Long menuId = 2L;
		private final int patchOrder = 2;

		@Test
		void 메뉴_순서_수정_성공() {
			// given
			MenuInfo patchMenuInfo = CUSTOM_MENU_INFO(menuId, patchOrder, GENERAL_MENU_NAME,
				GENERAL_PRICE, GENERAL_DESCRIPTION, GENERAL_IMAGE_URL, GENERAL_IS_SOLD_OUT, GENERAL_IS_RECOMMENDED);
			BDDMockito.given(menuWriter.patchMenuOrder(storeId, menuCategoryId, menuId, patchOrder))
				.willReturn(patchMenuInfo);

			// when
			MenuInfo servicePatchMenuInfo = menuService.patchMenuOrder(storeId, userPassport, menuCategoryId, menuId,
				patchOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(servicePatchMenuInfo.getMenuId()).isEqualTo(menuId);
				softly.assertThat(servicePatchMenuInfo.getMenuOrder()).isEqualTo(patchOrder);
				softly.assertThat(servicePatchMenuInfo.getMenuName()).isEqualTo(patchMenuInfo.getMenuName());
				softly.assertThat(servicePatchMenuInfo.getPrice()).isEqualTo(patchMenuInfo.getPrice());
				softly.assertThat(servicePatchMenuInfo.getDescription()).isEqualTo(patchMenuInfo.getDescription());
				softly.assertThat(servicePatchMenuInfo.getImageUrl()).isEqualTo(patchMenuInfo.getImageUrl());
				softly.assertThat(servicePatchMenuInfo.isSoldOut()).isEqualTo(patchMenuInfo.isSoldOut());
				softly.assertThat(servicePatchMenuInfo.isRecommended()).isEqualTo(patchMenuInfo.isRecommended());
			});
		}

		@Test
		void 주점_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenuOrder(storeId, userPassport, menuCategoryId, menuId, patchOrder))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(menuId);
				verify(menuWriter, never())
					.patchMenuOrder(storeId, menuCategoryId, menuId, patchOrder);
			});
		}

		@Test
		void 점주_인증_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenuOrder(storeId, userPassport, menuCategoryId, menuId, patchOrder))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(menuId);
				verify(menuWriter, never())
					.patchMenuOrder(storeId, menuCategoryId, menuId, patchOrder);
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_NOT_FOUND))
				.when(menuValidator)
				.validateMenu(menuId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.patchMenuOrder(storeId, userPassport, menuCategoryId, menuId, patchOrder))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator)
					.validateMenu(menuId);
				verify(menuWriter, never())
					.patchMenuOrder(storeId, menuCategoryId, menuId, patchOrder);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 삭제")
	class deleteMenu {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final Long menuCategoryId = 3L;
		private final Long menuId = 4L;

		@Test
		void 메뉴_삭제_성공() {
			// when
			menuService.deleteMenu(storeId, userPassport, menuCategoryId, menuId);

			// then
			verify(menuWriter)
				.deleteMenu(storeId, menuCategoryId, menuId);
		}

		@Test
		void 주점_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.deleteMenu(storeId, userPassport, menuCategoryId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(menuId);
				verify(menuWriter, never())
					.deleteMenu(storeId, menuCategoryId, menuId);
			});
		}

		@Test
		void 점주_인증_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.deleteMenu(storeId, userPassport, menuCategoryId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator, never())
					.validateMenu(menuId);
				verify(menuWriter, never())
					.deleteMenu(storeId, menuCategoryId, menuId);
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_NOT_FOUND))
				.when(menuValidator)
				.validateMenu(menuId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuService.deleteMenu(storeId, userPassport, menuCategoryId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuValidator)
					.validateMenu(menuId);
				verify(menuWriter, never())
					.deleteMenu(storeId, menuCategoryId, menuId);
			});
		}
	}
}
