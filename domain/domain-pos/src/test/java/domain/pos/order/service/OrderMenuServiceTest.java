package domain.pos.order.service;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.implement.OrderMenuReader;
import domain.pos.order.implement.OrderMenuWriter;
import domain.pos.order.implement.OrderReader;
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
	private MenuReader menuReader;
	@Mock
	private OrderMenuReader orderMenuReader;
	@Mock
	private OrderMenuWriter orderMenuWriter;

	@InjectMocks
	private OrderMenuService orderMenuService;

	@Nested
	@DisplayName("주문 메뉴 추가")
	class postOrderMenu {
		private final Long orderId = 1L;
		private final UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();
		private final OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();

		@Test
		void 주문_메뉴_추가_성공() {
			// given
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.of(OrderFixture.GENERAL_ORDER()));
			BDDMockito.given(menuReader.getMenuInfo(any(), any()))
				.willReturn(Optional.of(MenuInfoFixture.GENERAL_MENU_INFO()));

			// when
			orderMenuService.postOrderMenu(orderId, userPassport, orderMenu);

			// then
			verify(orderReader).getOrderWithCustomerAndOwner(orderId);
			verify(receiptValidator).validateIsOwner(any(), eq(userPassport));
			verify(menuReader).getMenuInfo(any(), any());
			verify(orderMenuWriter).postOrderMenu(orderId, orderMenu);
		}

		@Test
		void 주문_조회_실패() {
			// given
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, userPassport, orderMenu))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator, never()).validateIsOwner(any(), any());
				verify(menuReader, never()).getMenuInfo(any(), any());
				verify(orderMenuWriter, never()).postOrderMenu(any(), any());
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.of(OrderFixture.GENERAL_ORDER()));

			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateIsOwner(any(), any());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, userPassport, orderMenu))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator).validateIsOwner(any(), any());
				verify(menuReader, never()).getMenuInfo(any(), any());
				verify(orderMenuWriter, never()).postOrderMenu(any(), any());
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.of(OrderFixture.GENERAL_ORDER()));

			BDDMockito.given(menuReader.getMenuInfo(anyLong(), anyLong()))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderMenuService.postOrderMenu(orderId, userPassport, orderMenu))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator).validateIsOwner(any(), any());
				verify(menuReader).getMenuInfo(anyLong(), anyLong());
				verify(orderMenuWriter, never()).postOrderMenu(any(), any());
			});
		}
	}

	@Nested
	@DisplayName("주문 메뉴 수량 변경")
	class patchOrderMenuQuantity {
		private final Long orderMenuId = 1L;
		private final UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();
		private final Integer quantity = 2;

		@Test
		void 주문_메뉴_수량_변경_성공() {
			// given
			OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			// when
			orderMenuService.patchOrderMenuQuantity(orderMenuId, userPassport, quantity);

			// then
			verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
			verify(receiptValidator).validateIsOwner(any(), eq(userPassport));
			verify(orderMenuWriter).patchOrderMenuQuantity(orderMenu, quantity);
		}

		@Test
		void 주문_메뉴_조회_실패() {
			// given
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderMenuService.patchOrderMenuQuantity(orderMenuId, userPassport, quantity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_NOT_FOUND);

				verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
				verify(receiptValidator, never()).validateIsOwner(any(), any());
				verify(orderMenuWriter, never()).patchOrderMenuQuantity(any(), anyInt());
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateIsOwner(any(), any());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderMenuService.patchOrderMenuQuantity(orderMenuId, userPassport, quantity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
				verify(receiptValidator).validateIsOwner(any(), any());
				verify(orderMenuWriter, never()).patchOrderMenuQuantity(any(), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("주문 메뉴 삭제")
	class deleteOrderMenu {
		private final Long orderMenuId = 1L;
		private final UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

		@Test
		void 주문_메뉴_삭제_성공() {
			// given
			OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			// when
			orderMenuService.deleteOrderMenu(orderMenuId, userPassport);

			// then
			verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
			verify(receiptValidator).validateIsOwner(any(), eq(userPassport));
			verify(orderMenuWriter).deleteOrderMenu(orderMenuId);
		}

		@Test
		void 주문_메뉴_조회_실패() {
			// given
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderMenuService.deleteOrderMenu(orderMenuId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_MENU_NOT_FOUND);

				verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
				verify(receiptValidator, never()).validateIsOwner(any(), any());
				verify(orderMenuWriter, never()).deleteOrderMenu(any());
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			OrderMenu orderMenu = OrderMenuFixture.GENERAL_ORDER_MENU();
			BDDMockito.given(orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId))
				.willReturn(Optional.of(orderMenu));

			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateIsOwner(any(), any());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderMenuService.deleteOrderMenu(orderMenuId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);
				verify(orderMenuReader).getOrderMenuWithCustomerAndOwner(orderMenuId);
				verify(receiptValidator).validateIsOwner(any(), any());
				verify(orderMenuWriter, never()).deleteOrderMenu(any());
			});
		}
	}
}
