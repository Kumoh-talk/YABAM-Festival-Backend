package domain.pos.menu;

import static fixtures.member.UserFixture.*;
import static fixtures.menu.MenuCategoryFixture.*;
import static fixtures.menu.MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO;
import static fixtures.menu.MenuCategoryInfoFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuCategoryWriter;
import domain.pos.menu.service.MenuCategoryService;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;

public class MenuCategoryServiceTest extends ServiceTest {
	@Mock
	private StoreValidator storeValidator;
	@Mock
	private MenuCategoryValidator menuCategoryValidator;
	@Mock
	private StoreReader storeReader;
	@Mock
	private MenuCategoryWriter menuCategoryWriter;
	@Mock
	private MenuCategoryReader menuCategoryReader;
	@InjectMocks
	private MenuCategoryService menuCategoryService;

	@Nested
	@DisplayName("메뉴 카테고리 생성")
	class postMenuCategory {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();

		@Test
		void 메뉴_카테고리_생성_성공() {
			// given
			MenuCategory menuCategory = GENERAL_MENU_CATEGORY();

			BDDMockito.given(menuCategoryWriter.postMenuCategory(storeId, menuCategoryInfo))
				.willReturn(menuCategory);

			// when
			MenuCategory serviceMenuCategory = menuCategoryService.postMenuCategory(storeId, userPassport,
				menuCategoryInfo);

			// then
			assertSoftly(softly -> {
				MenuCategoryInfo serviceMenuCategoryInfo = serviceMenuCategory.getMenuCategoryInfo();

				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryId()).isEqualTo(GENERAL_MENU_CATEGORY_ID);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryName()).isEqualTo(GENERAL_MENU_CATEGORY_NAME);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryOrder())
					.isEqualTo(GENERAL_MENU_CATEGORY_ORDER);
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
						() -> menuCategoryService.postMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategoryOrder(storeId, menuCategoryInfo);
				verify(menuCategoryWriter, never())
					.postMenuCategory(storeId, menuCategoryInfo);
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
						() -> menuCategoryService.postMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategoryOrder(storeId, menuCategoryInfo);
				verify(menuCategoryWriter, never())
					.postMenuCategory(storeId, menuCategoryInfo);
			});
		}

		@Test
		void 중복된_메뉴_순서_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.EXIST_MENU_CATEGORY_ORDER))
				.when(menuCategoryValidator)
				.validateMenuCategoryOrder(storeId, menuCategoryInfo);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.postMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_MENU_CATEGORY_ORDER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategoryOrder(storeId, menuCategoryInfo);
				verify(menuCategoryWriter, never())
					.postMenuCategory(storeId, menuCategoryInfo);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 리스트 조회")
	class getMenuCategoryList {
		private final Long storeId = 1L;

		@Test
		void 메뉴_카테고리_리스트_조회_성공() {
			// given
			MenuCategory menuCategory = GENERAL_MENU_CATEGORY();

			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.of(CUSTOM_STORE(storeId, null, null)));
			BDDMockito.given(menuCategoryReader.getMenuCategoryList(storeId))
				.willReturn(List.of(menuCategory));

			// when
			List<MenuCategory> menuCategoryList = menuCategoryService.getMenuCategoryList(storeId);

			// then
			assertSoftly(softly -> {
				MenuCategoryInfo serviceMenuCategoryInfo = menuCategoryList.get(0).getMenuCategoryInfo();

				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryId()).isEqualTo(GENERAL_MENU_CATEGORY_ID);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryName()).isEqualTo(GENERAL_MENU_CATEGORY_NAME);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryOrder())
					.isEqualTo(GENERAL_MENU_CATEGORY_ORDER);
			});
		}

		@Test
		void 가게_조회_실패() {
			// given
			BDDMockito.given(storeReader.readSingleStore(storeId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> menuCategoryService.getMenuCategoryList(storeId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeReader)
					.readSingleStore(storeId);
				verify(menuCategoryReader, never())
					.getMenuCategoryList(storeId);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 수정")
	class patchMenuCategory {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();

		@Test
		void 메뉴_카테고리_수정_성공() {
			// given
			BDDMockito.given(menuCategoryWriter.patchMenuCategory(menuCategoryInfo))
				.willReturn(menuCategoryInfo);

			// when
			MenuCategoryInfo serviceMenuCategoryInfo = menuCategoryService.patchMenuCategory(storeId, userPassport,
				menuCategoryInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryId()).isEqualTo(GENERAL_MENU_CATEGORY_ID);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryName()).isEqualTo(GENERAL_MENU_CATEGORY_NAME);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryOrder())
					.isEqualTo(GENERAL_MENU_CATEGORY_ORDER);
			});
		}

		@Test
		void 가게_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.patchMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
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
						() -> menuCategoryService.patchMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
			});
		}

		@Test
		void 메뉴_카테고리_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator)
				.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.patchMenuCategory(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 순서 수정")
	class patchMenuCategoryOrder {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final MenuCategoryInfo menuCategoryInfo = GENERAL_MENU_CATEGORY_INFO();

		@Test
		void 메뉴_카테고리_수정_성공() {
			// given
			BDDMockito.given(menuCategoryWriter.patchMenuCategoryOrder(storeId, menuCategoryInfo))
				.willReturn(menuCategoryInfo);

			// when
			MenuCategoryInfo serviceMenuCategoryInfo = menuCategoryService.patchMenuCategoryOrder(storeId, userPassport,
				menuCategoryInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryId()).isEqualTo(GENERAL_MENU_CATEGORY_ID);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryName()).isEqualTo(GENERAL_MENU_CATEGORY_NAME);
				softly.assertThat(serviceMenuCategoryInfo.getMenuCategoryOrder())
					.isEqualTo(GENERAL_MENU_CATEGORY_ORDER);
			});
		}

		@Test
		void 가게_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.patchMenuCategoryOrder(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
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
						() -> menuCategoryService.patchMenuCategoryOrder(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
			});
		}

		@Test
		void 메뉴_카테고리_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator)
				.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.patchMenuCategoryOrder(storeId, userPassport, menuCategoryInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(menuCategoryInfo.getMenuCategoryId());
				verify(menuCategoryWriter, never())
					.patchMenuCategory(menuCategoryInfo);
			});
		}
	}

	@Nested
	@DisplayName("메뉴 카테고리 삭제")
	class deleteMenuCategory {
		private final Long storeId = 1L;
		private final UserPassport userPassport = OWNER_USER_PASSPORT();
		private final Long categoryId = 3L;

		@Test
		void 메뉴_카테고리_삭제_성공() {
			// when
			menuCategoryService.deleteMenuCategory(storeId, userPassport, categoryId);

			// then
			verify(menuCategoryWriter)
				.deleteMenuCategory(categoryId);

		}

		@Test
		void 가게_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator)
				.validateStoreOwner(userPassport, storeId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.deleteMenuCategory(storeId, userPassport, categoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(categoryId);
				verify(menuCategoryWriter, never())
					.deleteMenuCategory(categoryId);
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
						() -> menuCategoryService.deleteMenuCategory(storeId, userPassport, categoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_STORE_OWNER);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator, never())
					.validateMenuCategory(categoryId);
				verify(menuCategoryWriter, never())
					.deleteMenuCategory(categoryId);
			});
		}

		@Test
		void 메뉴_카테고리_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND))
				.when(menuCategoryValidator)
				.validateMenuCategory(categoryId);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> menuCategoryService.deleteMenuCategory(storeId, userPassport, categoryId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_CATEGORY_NOT_FOUND);
				verify(storeValidator)
					.validateStoreOwner(userPassport, storeId);
				verify(menuCategoryValidator)
					.validateMenuCategory(categoryId);
				verify(menuCategoryWriter, never())
					.deleteMenuCategory(categoryId);
			});
		}
	}
}
