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

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryWriter;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import fixtures.member.UserFixture;
import fixtures.menu.MenuCategoryFixture;
import fixtures.menu.MenuCategoryInfoFixture;
import fixtures.store.StoreFixture;

public class MenuCategoryServiceTest extends ServiceTest {
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private StoreReader storeReader;
	@Mock
	private MenuCategoryWriter menuCategoryWriter;
	@Mock
	private MenuCategoryReader menuCategoryReader;

	@InjectMocks
	private MenuCategoryService menuCategoryService;

	private final Long storeId = 1L;
	private final UserPassport ownerPassport = UserFixture.OWNER_USER_PASSPORT();

	@Nested
	@DisplayName("메뉴 카테고리 생성")
	class postMenuCategory {
		private final MenuCategoryInfo categoryInfo = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			MenuCategory menuCategory = MenuCategoryFixture.GENERAL_MENU_CATEGORY();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryWriter.postMenuCategory(store, categoryInfo)).willReturn(menuCategory);

			MenuCategory result = menuCategoryService.postMenuCategory(storeId, ownerPassport, categoryInfo);

			assertThat(result).isEqualTo(menuCategory);
			verify(menuCategoryWriter).postMenuCategory(store, categoryInfo);
		}

		@Test
		void 실패_가게_운영중() {
			Store openStore = StoreFixture.GENERAL_OPEN_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(openStore);

			assertThatThrownBy(() -> menuCategoryService.postMenuCategory(storeId, ownerPassport, categoryInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_IS_OPEN_MENU_WRITE);

			verify(menuCategoryWriter, never()).postMenuCategory(any(), any());
		}

		@Test
		void 실패_점주_불일치() {
			doThrow(new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER))
				.when(storeValidator).validateStoreOwner(ownerPassport, storeId);

			assertThatThrownBy(() -> menuCategoryService.postMenuCategory(storeId, ownerPassport, categoryInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);

			verify(menuCategoryWriter, never()).postMenuCategory(any(), any());
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 목록 조회")
	class getMenuCategoryList {
		@Test
		void 성공() {
			List<MenuCategoryInfo> infos = List.of(MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO());
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.of(StoreFixture.GENERAL_CLOSE_STORE()));
			given(menuCategoryReader.getMenuCategoryInfoList(storeId)).willReturn(infos);

			List<MenuCategoryInfo> result = menuCategoryService.getMenuCategoryList(storeId);

			assertThat(result).isEqualTo(infos);
		}

		@Test
		void 실패_가게_없음() {
			given(storeReader.readSingleStore(storeId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> menuCategoryService.getMenuCategoryList(storeId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);

			verify(menuCategoryReader, never()).getMenuCategoryInfoList(any());
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 수정")
	class patchMenuCategory {
		private final MenuCategoryInfo patchInfo = MenuCategoryInfoFixture.PATCH_GENERAL_MENU_INFO();

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfo(storeId, patchInfo.getId()))
				.willReturn(Optional.of(MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO()));
			given(menuCategoryWriter.patchMenuCategory(patchInfo)).willReturn(patchInfo);

			menuCategoryService.patchMenuCategory(storeId, ownerPassport, patchInfo);

			verify(menuCategoryWriter).patchMenuCategory(patchInfo);
		}

		@Test
		void 실패_카테고리_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfo(storeId, patchInfo.getId()))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> menuCategoryService.patchMenuCategory(storeId, ownerPassport, patchInfo))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);

			verify(menuCategoryWriter, never()).patchMenuCategory(any());
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 삭제")
	class deleteMenuCategory {
		private final Long categoryId = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_ID;

		@Test
		void 성공() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfoWithStoreLock(storeId, categoryId))
				.willReturn(Optional.of(MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO()));

			menuCategoryService.deleteMenuCategory(storeId, ownerPassport, categoryId);

			verify(menuCategoryWriter).deleteMenuCategory(storeId, categoryId);
		}

		@Test
		void 실패_카테고리_없음() {
			Store store = StoreFixture.GENERAL_CLOSE_STORE();
			given(storeValidator.validateStoreOwner(ownerPassport, storeId)).willReturn(store);
			given(menuCategoryReader.getMenuCategoryInfoWithStoreLock(storeId, categoryId))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> menuCategoryService.deleteMenuCategory(storeId, ownerPassport, categoryId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);

			verify(menuCategoryWriter, never()).deleteMenuCategory(any(), any());
		}
	}
}
