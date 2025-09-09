package domain.pos.menu.implement.v2;

import static domain.pos.menu.implement.v2.MenuOrderAllocator.*;
import static fixtures.menu.v2.MenuFixture.*;
import static fixtures.menu.v2.ValidMenuState.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.Menu;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.menu.port.required.MenuRepository;

@ExtendWith(MockitoExtension.class)
class MenuOrderAllocatorTest {
	@Mock
	private MenuRepository menuRepository;

	@Mock
	private MenuCategoryRepository menuCategoryRepository;

	@InjectMocks
	private MenuOrderAllocator menuOrderAllocator;

	@Test
	void allocateNewMenuOrder_success() {
		// given
		Long CATEGORY_ID = VALID_MENU_CATEGORY_ID_1;
		given(menuRepository.readMaxMenuOrder(CATEGORY_ID)).willReturn(5);

		// when
		Integer newOrder = menuOrderAllocator.allocateNewMenuOrder(CATEGORY_ID);

		// then
		assertThat(newOrder).isEqualTo(6);
		verify(menuCategoryRepository).lockMenuCategory(CATEGORY_ID);
		verify(menuRepository).readMaxMenuOrder(CATEGORY_ID);
	}

	@Nested
	@DisplayName("relocationOrders 테스트")
	class RelocationOrdersTest {
		@Test
		@DisplayName("updateOrder가 currentOrder보다 클 때")
		void updateOrder_is_bigger_than_currentOrder() {
			// given
			Long menuId = 1L;
			Long categoryId = 1L;
			Integer currentOrder = 2;
			Integer updateOrder = 5;

			Menu menu = mock(Menu.class);
			given(menu.getId()).willReturn(menuId);
			given(menu.getMenuCategoryId()).willReturn(categoryId);
			given(menu.getOrder()).willReturn(currentOrder);

			Menu updateMenu = mock(Menu.class);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(Integer.MAX_VALUE);
			given(menuRepository.updateOrder(menu.getId(), updateOrder)).willReturn(updateMenu);

			// when
			Menu result = menuOrderAllocator.relocationOrders(menu, updateOrder);

			// then
			verify(menuCategoryRepository).lockMenuCategory(categoryId);
			verify(menuRepository).updateTemporaryOrder(menuId, TEMPORARY_ORDER);
			verify(menuRepository).decrementMenuOrdersInRange(categoryId, currentOrder + 1, updateOrder);
			verify(menuRepository).updateOrder(menuId, updateOrder);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 currentOrder보다 작을 때")
		void updateOrder_is_smaller_than_currentOrder() {
			// given
			Long menuId = 1L;
			Long categoryId = 1L;
			Integer currentOrder = 5;
			Integer updateOrder = 2;

			Menu menu = mock(Menu.class);
			given(menu.getId()).willReturn(menuId);
			given(menu.getMenuCategoryId()).willReturn(categoryId);
			given(menu.getOrder()).willReturn(currentOrder);

			Menu updateMenu = mock(Menu.class);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(Integer.MAX_VALUE);
			given(menuRepository.updateOrder(menu.getId(), updateOrder)).willReturn(updateMenu);

			// when
			Menu result = menuOrderAllocator.relocationOrders(menu, updateOrder);

			// then
			verify(menuCategoryRepository).lockMenuCategory(categoryId);
			verify(menuRepository).updateTemporaryOrder(menuId, TEMPORARY_ORDER);
			verify(menuRepository).incrementMenuOrdersInRange(categoryId, updateOrder, currentOrder - 1);
			verify(menuRepository).updateOrder(menuId, updateOrder);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 현재 최댓값 이상이면 ServiceException(DOMAIN_INVALID_MENU_ORDER)")
		void updateOrder_is_bigger_than_max() {
			// given
			Long categoryId = 1L;
			Integer updateOrder = 10;

			Menu menu = mock(Menu.class);
			given(menu.getMenuCategoryId()).willReturn(categoryId);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(5);

			// when -> then
			assertThatThrownBy(() -> menuOrderAllocator.relocationOrders(menu, updateOrder))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((ServiceException)ex).getErrorCode())
				.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_ORDER);

			verify(menuRepository).readMaxMenuOrder(categoryId);
		}
	}

	@Test
	void deleteMenu_success() {
		// given
		Menu menu = VALID_MENU();
		Long menuId = menu.getId();
		Long categoryId = menu.getMenuCategoryId();
		Integer currentOrder = menu.getOrder();

		// when
		menuOrderAllocator.delete(menu);

		// then
		verify(menuCategoryRepository).lockMenuCategory(categoryId);
		verify(menuRepository).updateOrder(menuId, null);
		verify(menuRepository).deleteMenu(menuId);
		verify(menuRepository).decrementMenuOrdersInRange(categoryId, currentOrder + 1, Integer.MAX_VALUE);
	}

}
