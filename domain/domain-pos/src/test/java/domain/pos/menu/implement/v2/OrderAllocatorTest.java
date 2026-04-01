package domain.pos.menu.implement.v2;

import static fixtures.menu.v2.MenuFixture.*;
import static fixtures.menu.v2.ValidMenuState.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.domain.Menu;

@ExtendWith(MockitoExtension.class)
class OrderAllocatorTest {

	@Mock
	OrderingOps<Menu> orderingOps;

	@InjectMocks
	OrderAllocator<Menu> orderAllocator;

	@Test
	void success() {
		Long menuCategoryId = VALID_MENU_CATEGORY_ID_1;
		given(orderingOps.readGuardMaxOrder(menuCategoryId)).willReturn(5);

		Integer newOrder = orderAllocator.allocateNewOrder(menuCategoryId);

		assertThat(newOrder).isEqualTo(6);
		verify(orderingOps).lockGuard(menuCategoryId);
		verify(orderingOps).readGuardMaxOrder(menuCategoryId);
	}

	@Nested
	@DisplayName("relocationOrders 테스트")
	class RelocationOrdersTest {
		@Test
		@DisplayName("updatedOrder가 previousOrder보다 클 때")
		void updatedOrder_is_bigger_than_previousOrder() {
			Long userId = 1L;
			Long storeId = 2L;
			Long menuId = 3L;
			Long menuCategoryId = 4L;
			Integer previousOrder = 2;
			Integer updatedOrder = 5;

			Menu updateMenu = mock(Menu.class);

			given(orderingOps.readGuardMaxOrder(menuCategoryId)).willReturn(Integer.MAX_VALUE);
			given(orderingOps.updateOrder(userId, storeId, menuId, updatedOrder)).willReturn(
				Optional.of(updateMenu));

			Menu result = orderAllocator.relocationOrders(userId, storeId, menuCategoryId, menuId, updatedOrder,
				previousOrder).get();

			verify(orderingOps).lockGuard(menuCategoryId);
			verify(orderingOps).updateToTemporaryOrder(menuId);
			verify(orderingOps).decrementOrdersInRange(menuCategoryId, previousOrder + 1, updatedOrder);
			verify(orderingOps).updateOrder(userId, storeId, menuId, updatedOrder);
			verify(orderingOps).bumpGuardVersion(menuCategoryId);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 currentOrder보다 작을 때")
		void updateOrder_is_smaller_than_currentOrder() {
			Long userId = 1L;
			Long storeId = 2L;
			Long menuId = 3L;
			Long menuCategoryId = 4L;
			Integer previousOrder = 5;
			Integer updatedOrder = 2;

			Menu updateMenu = mock(Menu.class);

			given(orderingOps.readGuardMaxOrder(menuCategoryId)).willReturn(Integer.MAX_VALUE);
			given(orderingOps.updateOrder(userId, storeId, menuId, updatedOrder)).willReturn(
				Optional.of(updateMenu));

			Menu result = orderAllocator.relocationOrders(userId, storeId, menuCategoryId, menuId, updatedOrder,
				previousOrder).get();

			verify(orderingOps).lockGuard(menuCategoryId);
			verify(orderingOps).updateToTemporaryOrder(menuId);
			verify(orderingOps).incrementOrdersInRange(menuCategoryId, updatedOrder, previousOrder - 1);
			verify(orderingOps).updateOrder(userId, storeId, menuId, updatedOrder);
			verify(orderingOps).bumpGuardVersion(menuCategoryId);
			assertThat(result).isSameAs(updateMenu);
		}

		@Test
		@DisplayName("updateOrder가 현재 최댓값 이상이면 ServiceException(DOMAIN_INVALID_MENU_ORDER)")
		void updateOrder_is_bigger_than_max() {
			Long userId = 1L;
			Long storeId = 2L;
			Long menuId = 3L;
			Long menuCategoryId = 4L;
			Integer previousOrder = 5;
			Integer updatedOrder = 10;

			given(orderingOps.readGuardMaxOrder(menuCategoryId)).willReturn(previousOrder);

			assertThatThrownBy(
				() -> orderAllocator.relocationOrders(userId, storeId, menuCategoryId, menuId, updatedOrder,
					previousOrder))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((ServiceException)ex).getErrorCode())
				.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_ORDER);

			verify(orderingOps).readGuardMaxOrder(menuCategoryId);
		}
	}

	@Test
	void deleteMenu_success() {
		Long userId = 1L;
		Long storeId = 2L;
		Menu menu = VALID_MENU();
		Long menuId = menu.getId();
		Long menuCategoryId = menu.getMenuCategoryId();
		Integer deletedOrder = menu.getOrder();

		given(orderingOps.refreshOrder(menuId)).willReturn(deletedOrder);

		orderAllocator.deleteOrder(userId, storeId, menuCategoryId, menuId);

		verify(orderingOps).lockGuard(menuCategoryId);
		verify(orderingOps).updateOrder(userId, storeId, menuId, null);
		verify(orderingOps).decrementOrdersInRange(menuCategoryId, deletedOrder + 1, Integer.MAX_VALUE);
		verify(orderingOps).bumpGuardVersion(menuCategoryId);
	}

}
