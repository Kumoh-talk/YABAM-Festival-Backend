package domain.pos.order.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.implement.OrderMenuReader;
import domain.pos.order.implement.OrderMenuWriter;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.implement.ReceiptValidator;
import fixtures.member.UserFixture;
import fixtures.menu.MenuInfoFixture;
import fixtures.order.OrderFixture;
import fixtures.order.OrderMenuFixture;

public class OrderMenuServiceTest extends ServiceTest {
	@Mock
	private ReceiptValidator receiptValidator;
	@Mock
	private OrderReader orderReader;
	@Mock
	private OrderWriter orderWriter;
	@Mock
	private MenuReader menuReader;
	@Mock
	private OrderMenuReader orderMenuReader;
	@Mock
	private OrderMenuWriter orderMenuWriter;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private OrderMenuService orderMenuService;

	@Nested
	@DisplayName("주문 메뉴 추가")
	class postOrderMenu {
		private final Long orderId = 1L;
		private final Long menuId = 1L;
		private final Integer quantity = 2;
		private final UserPassport ownerPassport = UserFixture.OWNER_USER_PASSPORT();

		@Test
		void 성공() {
			Order order = OrderFixture.RECEIVED_ORDER();
			MenuInfo menuInfo = MenuInfoFixture.GENERAL_MENU_INFO();
			given(orderReader.getOrderWithStoreAndMenusAndLock(orderId)).willReturn(Optional.of(order));
			given(menuReader.getMenuInfo(anyLong(), eq(menuId))).willReturn(Optional.of(menuInfo));

			orderMenuService.postOrderMenu(orderId, ownerPassport, menuId, quantity);

			verify(orderMenuWriter).postOrderMenu(menuInfo, quantity, order);
		}

		@Test
		void 실패_주문_없음() {
			given(orderReader.getOrderWithStoreAndMenusAndLock(orderId)).willReturn(Optional.empty());

			assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, ownerPassport, menuId, quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

			verify(orderMenuWriter, never()).postOrderMenu(any(), anyInt(), any());
		}

		@Test
		void 실패_주문_상태_RECEIVED_아님() {
			Order order = OrderFixture.GENERAL_ORDER();
			given(orderReader.getOrderWithStoreAndMenusAndLock(orderId)).willReturn(Optional.of(order));

			assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, ownerPassport, menuId, quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STATUS_NOT_RECEIVED);

			verify(orderMenuWriter, never()).postOrderMenu(any(), anyInt(), any());
		}

		@Test
		void 실패_점주_불일치() {
			Order order = OrderFixture.RECEIVED_ORDER();
			given(orderReader.getOrderWithStoreAndMenusAndLock(orderId)).willReturn(Optional.of(order));
			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateIsOwner(any(), any());

			assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, ownerPassport, menuId, quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

			verify(orderMenuWriter, never()).postOrderMenu(any(), anyInt(), any());
		}

		@Test
		void 실패_메뉴_없음() {
			Order order = OrderFixture.RECEIVED_ORDER();
			given(orderReader.getOrderWithStoreAndMenusAndLock(orderId)).willReturn(Optional.of(order));
			given(menuReader.getMenuInfo(anyLong(), eq(menuId))).willReturn(Optional.empty());

			assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, ownerPassport, menuId, quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

			verify(orderMenuWriter, never()).postOrderMenu(any(), anyInt(), any());
		}
	}

	@Nested
	@DisplayName("주문 메뉴 삭제")
	class deleteOrderMenu {
		private final Long orderMenuId = 1L;

		@Test
		void 성공() {
			OrderMenu orderMenu = receivedOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			orderMenuService.deleteOrderMenu(orderMenuId, UserFixture.OWNER_USER_PASSPORT());

			verify(orderMenuWriter).deleteOrderMenu(orderMenu);
			verify(eventPublisher).publishEvent((Object) any());
		}

		@Test
		void 실패_주문_메뉴_없음() {
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.empty());

			assertThatThrownBy(
				() -> orderMenuService.deleteOrderMenu(orderMenuId, UserFixture.OWNER_USER_PASSPORT()))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_NOT_FOUND);

			verify(orderMenuWriter, never()).deleteOrderMenu(any());
		}

		@Test
		void 실패_주문_상태_RECEIVED_COMPLETED_아님() {
			OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			assertThatThrownBy(
				() -> orderMenuService.deleteOrderMenu(orderMenuId, UserFixture.OWNER_USER_PASSPORT()))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STATUS_NOT_RECEIVED);

			verify(orderMenuWriter, never()).deleteOrderMenu(any());
		}

		@Test
		void 실패_점주_불일치() {
			OrderMenu orderMenu = receivedOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			assertThatThrownBy(
				() -> orderMenuService.deleteOrderMenu(orderMenuId, UserFixture.DIFF_OWNER_PASSPORT()))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_ACCESS_DENIED);

			verify(orderMenuWriter, never()).deleteOrderMenu(any());
		}
	}

	@Nested
	@DisplayName("주문 메뉴 상태 변경")
	class patchOrderMenuStatus {
		private final Long orderMenuId = 1L;

		@Test
		void 성공_CANCELED_이벤트_발행() {
			OrderMenu orderMenu = cookingOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));
			given(orderMenuWriter.patchOrderMenuStatus(any(), any(), any())).willReturn(orderMenu);

			orderMenuService.patchOrderMenuStatus(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
				OrderMenuStatus.CANCELED);

			verify(orderMenuWriter).patchOrderMenuStatus(eq(orderMenu), eq(OrderMenuStatus.CANCELED), any());
			verify(eventPublisher).publishEvent((Object) any());
		}

		@Test
		void 성공_RECEIVED_이벤트_미발행() {
			OrderMenu orderMenu = cookingOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));
			given(orderMenuWriter.patchOrderMenuStatus(any(), any(), any())).willReturn(orderMenu);

			orderMenuService.patchOrderMenuStatus(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
				OrderMenuStatus.COOKING);

			verify(orderMenuWriter).patchOrderMenuStatus(eq(orderMenu), eq(OrderMenuStatus.COOKING), any());
			verify(eventPublisher, never()).publishEvent((Object) any());
		}

		@Test
		void 실패_주문_메뉴_없음() {
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.empty());

			assertThatThrownBy(
				() -> orderMenuService.patchOrderMenuStatus(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
					OrderMenuStatus.CANCELED))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("주문 메뉴 수량 변경")
	class patchOrderMenuQuantity {
		private final Long orderMenuId = 1L;
		private final Integer quantity = 3;

		@Test
		void 성공() {
			OrderMenu orderMenu = cookingOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));
			given(orderMenuWriter.patchOrderMenuQuantity(orderMenu, quantity)).willReturn(orderMenu);

			orderMenuService.patchOrderMenuQuantity(orderMenuId, UserFixture.OWNER_USER_PASSPORT(), quantity);

			verify(orderMenuWriter).patchOrderMenuQuantity(orderMenu, quantity);
		}

		@Test
		void 실패_주문_메뉴_없음() {
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.empty());

			assertThatThrownBy(
				() -> orderMenuService.patchOrderMenuQuantity(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
					quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_NOT_FOUND);
		}

		@Test
		void 실패_상태_변경_불가() {
			OrderMenu orderMenu = orderedStatusOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			assertThatThrownBy(
				() -> orderMenuService.patchOrderMenuQuantity(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
					quantity))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_STATUS_NOT_ALLOWED);
		}
	}

	@Nested
	@DisplayName("주문 메뉴 완료 수량 변경")
	class patchOrderMenuCompletedCount {
		private final Long orderMenuId = 1L;

		@Test
		void 성공() {
			OrderMenu orderMenu = cookingOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));
			given(orderMenuWriter.patchOrderMenuCompletedCount(orderMenu, 1)).willReturn(orderMenu);

			orderMenuService.patchOrderMenuCompletedCount(orderMenuId, UserFixture.OWNER_USER_PASSPORT(), 1);

			verify(orderMenuWriter).patchOrderMenuCompletedCount(orderMenu, 1);
		}

		@Test
		void 실패_완료_수량_초과() {
			OrderMenu orderMenu = cookingOrderMenu();
			given(orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			assertThatThrownBy(
				() -> orderMenuService.patchOrderMenuCompletedCount(orderMenuId, UserFixture.OWNER_USER_PASSPORT(),
					orderMenu.getQuantity() + 1))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPLETED_COUNT_OVERFLOW);

			verify(orderMenuWriter, never()).patchOrderMenuCompletedCount(any(), anyInt());
		}
	}

	private OrderMenu receivedOrderMenu() {
		Order receivedOrder = OrderFixture.RECEIVED_ORDER();
		return OrderMenu.builder()
			.orderMenuId(OrderMenuFixture.GENERAL_ORDER_MENU_ID)
			.orderMenuStatus(OrderMenuStatus.COOKING)
			.quantity(OrderMenuFixture.GENERAL_ORDER_MENU_QUANTITY)
			.completedCount(OrderMenuFixture.GENERAL_ORDER_MENU_COMPLETED_COUNT)
			.order(receivedOrder)
			.menu(OrderMenuFixture.GENERAL_ORDER_MENU)
			.build();
	}

	private OrderMenu orderedStatusOrderMenu() {
		return OrderMenu.builder()
			.orderMenuId(OrderMenuFixture.GENERAL_ORDER_MENU_ID)
			.orderMenuStatus(OrderMenuStatus.ORDERED)
			.quantity(OrderMenuFixture.GENERAL_ORDER_MENU_QUANTITY)
			.completedCount(OrderMenuFixture.GENERAL_ORDER_MENU_COMPLETED_COUNT)
			.order(OrderMenuFixture.GENERAL_ORDER)
			.menu(OrderMenuFixture.GENERAL_ORDER_MENU)
			.build();
	}

	private OrderMenu cookingOrderMenu() {
		return OrderMenu.builder()
			.orderMenuId(OrderMenuFixture.GENERAL_ORDER_MENU_ID)
			.orderMenuStatus(OrderMenuStatus.COOKING)
			.quantity(OrderMenuFixture.GENERAL_ORDER_MENU_QUANTITY)
			.completedCount(OrderMenuFixture.GENERAL_ORDER_MENU_COMPLETED_COUNT)
			.order(OrderMenuFixture.GENERAL_ORDER)
			.menu(OrderMenuFixture.GENERAL_ORDER_MENU)
			.build();
	}
}
