package domain.pos.menu.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuReader;
import domain.pos.menu.implement.MenuWriter;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import fixtures.member.UserFixture;
import fixtures.menu.MenuCategoryInfoFixture;
import fixtures.menu.MenuFixture;
import fixtures.menu.MenuInfoFixture;
import fixtures.store.StoreFixture;

public class MenuServiceTest extends ServiceTest {
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private MenuCategoryValidator menuCategoryValidator;
	@Mock
	private StoreReader storeReader;
	@Mock
	private MenuCategoryReader menuCategoryReader;
	@Mock
	private MenuReader menuReader;
	@Mock
	private MenuWriter menuWriter;

	@InjectMocks
	private MenuService menuService;

	private final Long storeId = 1L;
	private final UserPassport ownerPassport = UserFixture.OWNER_USER_PASSPORT();

	@Nested
	@DisplayName("메뉴 생성")
	class postMenu {
		private final Long categoryId = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_ID;
		private final MenuInfo menuInfo = MenuInfoFixture.REQUEST_MENU_INFO();

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			MenuCategoryInfo categoryInfo = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();
			Menu menu = MenuFixture.GENERAL_MENU();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfo(storeId, categoryId)).willReturn(Optional.of(categoryInfo));
			given(menuWriter.postMenu(store, categoryInfo, menuInfo)).willReturn(menu);

			Menu result = menuService.postMenu(storeId, ownerPassport, categoryId, menuInfo);

			assertThat(result).isEqualTo(menu);
			verify(menuWriter).postMenu(store, categoryInfo, menuInfo);
		}

		@Test
		void 실패_가게_운영중() {
			Store openStore = StoreFixture.GENERAL_OPEN_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(openStore);

			assertThatThrownBy(() -> menuService.postMenu(storeId, ownerPassport, categoryId, menuInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_MENU_WRITE);

			verify(menuWriter, never()).postMenu(any(), any(), any());
		}

		@Test
		void 실패_점주_불일치() {
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(ownerPassport, storeId);

			assertThatThrownBy(() -> menuService.postMenu(storeId, ownerPassport, categoryId, menuInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

			verify(menuWriter, never()).postMenu(any(), any(), any());
		}

		@Test
		void 실패_카테고리_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfo(storeId, categoryId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.postMenu(storeId, ownerPassport, categoryId, menuInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);

			verify(menuWriter, never()).postMenu(any(), any(), any());
		}
	}

	@Nested
	@DisplayName("메뉴 단일 조회")
	class getMenuInfo {
		private final Long menuId = MenuInfoFixture.GENERAL_MENU_ID;

		@Test
		void 성공() {
			MenuInfo menuInfo = MenuInfoFixture.GENERAL_MENU_INFO();
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuReader.getMenuInfo(storeId, menuId)).willReturn(Optional.of(menuInfo));

			MenuInfo result = menuService.getMenuInfo(storeId, menuId);

			assertThat(result).isEqualTo(menuInfo);
		}

		@Test
		void 실패_가게_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

			verify(menuReader, never()).getMenuInfo(any(), any());
		}

		@Test
		void 실패_메뉴_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuReader.getMenuInfo(storeId, menuId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getMenuInfo(storeId, menuId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("메뉴 슬라이스 조회")
	class getMenuSlice {
		private final int pageSize = 10;

		@Test
		void 성공() {
			Slice<Menu> slice = new SliceImpl<>(List.of(MenuFixture.GENERAL_MENU()));
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuReader.getMenuSlice(pageSize, storeId, null, null)).willReturn(slice);

			Slice<Menu> result = menuService.getMenuSlice(pageSize, null, storeId, null);

			assertThat(result.getContent()).hasSize(1);
		}

		@Test
		void 실패_가게_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getMenuSlice(pageSize, null, storeId, null))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
		}

		@Test
		void 실패_카테고리_없음() {
			Long lastCategoryId = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_ID;
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuCategoryReader.getMenuCategoryInfo(storeId, lastCategoryId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getMenuSlice(pageSize, null, storeId, lastCategoryId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
		}

		@Test
		void 실패_메뉴_없음() {
			Long lastMenuId = MenuInfoFixture.GENERAL_MENU_ID;
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuReader.getMenuInfo(storeId, lastMenuId, null)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getMenuSlice(pageSize, lastMenuId, storeId, null))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("카테고리 메뉴 목록 조회")
	class getCategoryMenuList {
		private final Long categoryId = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_ID;

		@Test
		void 성공() {
			List<MenuInfo> menuInfos = List.of(MenuInfoFixture.GENERAL_MENU_INFO());
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuReader.getCategoryMenuList(storeId, categoryId)).willReturn(menuInfos);

			List<MenuInfo> result = menuService.getCategoryMenuList(storeId, categoryId);

			assertThat(result).isEqualTo(menuInfos);
		}

		@Test
		void 실패_가게_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.getCategoryMenuList(storeId, categoryId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

			verify(menuCategoryValidator, never()).validateMenuCategory(any(), any());
		}

		@Test
		void 실패_카테고리_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator).validateMenuCategory(storeId, categoryId);

			assertThatThrownBy(() -> menuService.getCategoryMenuList(storeId, categoryId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);

			verify(menuReader, never()).getCategoryMenuList(any(), any());
		}
	}

	@Nested
	@DisplayName("메뉴 정보 수정")
	class patchMenuInfo {
		private final MenuInfo patchInfo = MenuInfoFixture.GENERAL_MENU_INFO();

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuInfo(storeId, patchInfo.getId())).willReturn(Optional.of(patchInfo));
			given(menuWriter.patchMenuInfo(patchInfo)).willReturn(patchInfo);

			menuService.patchMenuInfo(storeId, ownerPassport, patchInfo);

			verify(menuWriter).patchMenuInfo(patchInfo);
		}

		@Test
		void 실패_가게_운영중() {
			given(storeValidator.validateStoreOwner(ownerPassport, storeId))
				.willReturn(StoreFixture.GENERAL_OPEN_STORE());

			assertThatThrownBy(() -> menuService.patchMenuInfo(storeId, ownerPassport, patchInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_MENU_WRITE);

			verify(menuWriter, never()).patchMenuInfo(any());
		}

		@Test
		void 실패_점주_불일치() {
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(ownerPassport, storeId);

			assertThatThrownBy(() -> menuService.patchMenuInfo(storeId, ownerPassport, patchInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

			verify(menuWriter, never()).patchMenuInfo(any());
		}

		@Test
		void 실패_메뉴_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuInfo(storeId, patchInfo.getId())).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.patchMenuInfo(storeId, ownerPassport, patchInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

			verify(menuWriter, never()).patchMenuInfo(any());
		}
	}

	@Nested
	@DisplayName("메뉴 순서 수정")
	class patchMenuOrder {
		private final Long menuId = MenuInfoFixture.GENERAL_MENU_ID;
		private final Integer patchOrder = 3;

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			Menu menu = MenuFixture.GENERAL_MENU();
			MenuInfo patchedInfo = MenuInfoFixture.GENERAL_MENU_INFO();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)).willReturn(Optional.of(menu));
			given(menuWriter.patchMenuOrder(menu, patchOrder)).willReturn(patchedInfo);

			menuService.patchMenuOrder(storeId, ownerPassport, menuId, patchOrder);

			verify(menuWriter).patchMenuOrder(menu, patchOrder);
		}

		@Test
		void 실패_가게_운영중() {
			given(storeValidator.validateStoreOwner(ownerPassport, storeId))
				.willReturn(StoreFixture.GENERAL_OPEN_STORE());

			assertThatThrownBy(() -> menuService.patchMenuOrder(storeId, ownerPassport, menuId, patchOrder))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_MENU_WRITE);

			verify(menuWriter, never()).patchMenuOrder(any(), any());
		}

		@Test
		void 실패_메뉴_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.patchMenuOrder(storeId, ownerPassport, menuId, patchOrder))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

			verify(menuWriter, never()).patchMenuOrder(any(), any());
		}
	}

	@Nested
	@DisplayName("메뉴 품절 여부 수정")
	class patchIsSoldOut {
		private final Long menuId = MenuInfoFixture.GENERAL_MENU_ID;

		@Test
		void 성공() {
			MenuInfo menuInfo = MenuInfoFixture.GENERAL_MENU_INFO();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId))
				.willReturn(StoreFixture.GENERAL_CLOSE_STORE());
			given(menuReader.getMenuInfo(storeId, menuId)).willReturn(Optional.of(menuInfo));
			given(menuWriter.patchMenuInfo(menuInfo)).willReturn(menuInfo);

			menuService.patchIsSoldOut(storeId, ownerPassport, menuId, true);

			verify(menuWriter).patchMenuInfo(menuInfo);
		}

		@Test
		void 실패_점주_불일치() {
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(ownerPassport, storeId);

			assertThatThrownBy(() -> menuService.patchIsSoldOut(storeId, ownerPassport, menuId, true))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

			verify(menuWriter, never()).patchMenuInfo(any());
		}

		@Test
		void 실패_메뉴_없음() {
			given(storeValidator.validateStoreOwner(ownerPassport, storeId))
				.willReturn(StoreFixture.GENERAL_CLOSE_STORE());
			given(menuReader.getMenuInfo(storeId, menuId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.patchIsSoldOut(storeId, ownerPassport, menuId, true))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

			verify(menuWriter, never()).patchMenuInfo(any());
		}
	}

	@Nested
	@DisplayName("메뉴 삭제")
	class deleteMenu {
		private final Long menuId = MenuInfoFixture.GENERAL_MENU_ID;

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			Menu menu = MenuFixture.GENERAL_MENU();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)).willReturn(Optional.of(menu));

			menuService.deleteMenu(storeId, ownerPassport, menuId);

			verify(menuWriter).deleteMenu(menu);
		}

		@Test
		void 실패_가게_운영중() {
			given(storeValidator.validateStoreOwner(ownerPassport, storeId))
				.willReturn(StoreFixture.GENERAL_OPEN_STORE());

			assertThatThrownBy(() -> menuService.deleteMenu(storeId, ownerPassport, menuId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_MENU_WRITE);

			verify(menuWriter, never()).deleteMenu(any());
		}

		@Test
		void 실패_점주_불일치() {
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(ownerPassport, storeId);

			assertThatThrownBy(() -> menuService.deleteMenu(storeId, ownerPassport, menuId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

			verify(menuWriter, never()).deleteMenu(any());
		}

		@Test
		void 실패_메뉴_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuService.deleteMenu(storeId, ownerPassport, menuId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

			verify(menuWriter, never()).deleteMenu(any());
		}
	}
}
