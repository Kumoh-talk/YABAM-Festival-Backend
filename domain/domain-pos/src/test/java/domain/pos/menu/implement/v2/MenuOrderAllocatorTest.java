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
		@DisplayName("updatedOrder가 previousOrder보다 클 때")
		void updatedOrder_is_bigger_than_previousOrder() {
			// given
			Long menuId = 1L;
			Long categoryId = 1L;
			Integer previousOrder = 2;
			Integer updatedOrder = 5;

			Menu menu = mock(Menu.class);
			given(menu.getId()).willReturn(menuId);
			given(menu.getMenuCategoryId()).willReturn(categoryId);
			given(menu.getOrder()).willReturn(updatedOrder);

			Menu updateMenu = mock(Menu.class);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(Integer.MAX_VALUE);
			given(menuRepository.updateOrder(menu.getId(), updatedOrder)).willReturn(updateMenu);

			// when
			Menu result = menuOrderAllocator.relocationOrders(menu, previousOrder);

			// then
			verify(menuCategoryRepository).lockMenuCategory(categoryId);
			verify(menuRepository).updateTemporaryOrder(menuId, TEMPORARY_ORDER);
			verify(menuRepository).decrementMenuOrdersInRange(categoryId, previousOrder + 1, updatedOrder);
			verify(menuRepository).updateOrder(menuId, updatedOrder);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 currentOrder보다 작을 때")
		void updateOrder_is_smaller_than_currentOrder() {
			// given
			Long menuId = 1L;
			Long categoryId = 1L;
			Integer previousOrder = 5;
			Integer updatedOrder = 2;

			Menu menu = mock(Menu.class);
			given(menu.getId()).willReturn(menuId);
			given(menu.getMenuCategoryId()).willReturn(categoryId);
			given(menu.getOrder()).willReturn(updatedOrder);

			Menu updateMenu = mock(Menu.class);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(Integer.MAX_VALUE);
			given(menuRepository.updateOrder(menu.getId(), updatedOrder)).willReturn(updateMenu);

			// when
			Menu result = menuOrderAllocator.relocationOrders(menu, previousOrder);

			// then
			verify(menuCategoryRepository).lockMenuCategory(categoryId);
			verify(menuRepository).updateTemporaryOrder(menuId, TEMPORARY_ORDER);
			verify(menuRepository).incrementMenuOrdersInRange(categoryId, updatedOrder, previousOrder - 1);
			verify(menuRepository).updateOrder(menuId, updatedOrder);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 현재 최댓값 이상이면 ServiceException(DOMAIN_INVALID_MENU_ORDER)")
		void updateOrder_is_bigger_than_max() {
			// given
			Long categoryId = 1L;
			Integer previousOrder = 5;
			Integer updatedOrder = 10;

			Menu menu = mock(Menu.class);
			given(menu.getMenuCategoryId()).willReturn(categoryId);
			given(menu.getOrder()).willReturn(updatedOrder);

			given(menuRepository.readMaxMenuOrder(menu.getMenuCategoryId())).willReturn(previousOrder);

			// when -> then
			assertThatThrownBy(() -> menuOrderAllocator.relocationOrders(menu, previousOrder))
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
		Integer deletedOrder = menu.getOrder();
		
		given(menuRepository.refrsh(menu)).willReturn(menu);

		// when
		menuOrderAllocator.delete(menu);

		// then
		verify(menuCategoryRepository).lockMenuCategory(categoryId);
		verify(menuRepository).updateOrder(menuId, null);
		verify(menuRepository).deleteMenu(menuId);
		verify(menuRepository).decrementMenuOrdersInRange(categoryId, deletedOrder + 1, Integer.MAX_VALUE);
	}

}
