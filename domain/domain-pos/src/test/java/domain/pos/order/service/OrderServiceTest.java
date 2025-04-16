package domain.pos.order.service;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import fixtures.member.UserFixture;
import fixtures.menu.MenuInfoFixture;
import fixtures.order.OrderFixture;
import fixtures.order.OrderMenuFixture;
import fixtures.receipt.ReceiptFixture;
import fixtures.receipt.ReceiptInfoFixture;

public class OrderServiceTest extends ServiceTest {
	@Mock
	private ReceiptValidator receiptValidator;

	@Mock
	private ReceiptReader receiptReader;
	@Mock
	private MenuReader menuReader;
	@Mock
	private OrderWriter orderWriter;
	@Mock
	private OrderReader orderReader;

	@InjectMocks
	private OrderService orderService;

	@Nested
	@DisplayName("주문 등록")
	class postOrder {
		private Long receiptId = ReceiptInfoFixture.NON_ADJUSTMENT_RECEIPT_INFO().getReceiptId();
		private UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();
		private List<OrderMenu> orderMenus = List.of(OrderMenuFixture.GENERAL_ORDER_MENU());

		@Test
		void 주문_등록_성공() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			List<Long> orderMenuIds = orderMenus.stream()
				.map(orderMenu -> orderMenu.getMenu().getMenuInfo().getMenuId())
				.toList();

			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));
			BDDMockito.given(
					menuReader.getMenuInfo(
						BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
						ArgumentMatchers.argThat(orderMenuIds::contains)))
				.willReturn(Optional.of(MenuInfoFixture.GENERAL_MENU_INFO()));

			// when
			orderService.postOrder(receiptId, userPassport, orderMenus);

			// then
			verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
			verify(receiptValidator).validateAccessToReceipt(receipt, userPassport);
			verify(menuReader).getMenuInfo(
				BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
				ArgumentMatchers.argThat(orderMenuIds::contains));
			verify(orderWriter).postOrder(receiptId, orderMenus);
		}

		@Test
		void 영수증_조회_실패() {
			// given
			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.postOrder(receiptId, userPassport, orderMenus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
				verify(receiptValidator, never()).validateAccessToReceipt(any(), any());
				verify(menuReader, never()).getMenuInfo(anyLong(), any());
				verify(orderWriter, never()).postOrder(receiptId, orderMenus);
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));
			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateAccessToReceipt(receipt, userPassport);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.postOrder(receiptId, userPassport, orderMenus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
				verify(receiptValidator).validateAccessToReceipt(receipt, userPassport);
				verify(menuReader, never()).getMenuInfo(anyLong(), any());
				verify(orderWriter, never()).postOrder(receiptId, orderMenus);
			});
		}

		@Test
		void 메뉴_조회_실패() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			List<Long> orderMenuIds = orderMenus.stream()
				.map(orderMenu -> orderMenu.getMenu().getMenuInfo().getMenuId())
				.toList();

			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));
			BDDMockito.given(
					menuReader.getMenuInfo(
						BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
						ArgumentMatchers.argThat(orderMenuIds::contains)))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.postOrder(receiptId, userPassport, orderMenus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
				verify(receiptValidator).validateAccessToReceipt(receipt, userPassport);
				verify(menuReader).getMenuInfo(
					BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
					ArgumentMatchers.argThat(orderMenuIds::contains));
				verify(orderWriter, never()).postOrder(receiptId, orderMenus);
			});
		}
	}

	@Nested
	@DisplayName("주문 상태 변경")
	class patchOrderStatus {
		private Long orderId = OrderFixture.GENERAL_ORDER_ID;
		private UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

		private OrderStatus orderStatus = OrderStatus.RECEIVED;

		@Test
		void 주문_상태_변경_성공() {
			// given
			Order order = OrderFixture.GENERAL_ORDER();

			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.of(order));

			// when
			orderService.patchOrderStatus(orderId, userPassport, orderStatus);

			// then
			verify(orderReader).getOrderWithCustomerAndOwner(orderId);
			verify(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);
			verify(orderWriter).patchOrderStatus(order, orderStatus, userPassport.getUserRole());
		}

		@Test
		void 주문_조회_실패() {
			// given
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.empty());

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.patchOrderStatus(orderId, userPassport, orderStatus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator, never()).validateAccessToReceipt(any(), any());
				verify(orderWriter, never()).patchOrderStatus(any(), any(), any());
			});
		}

		@Test
		void 주문_접근_권한_실패() {
			// given
			Order order = OrderFixture.GENERAL_ORDER();
			BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
				.willReturn(Optional.of(order));
			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.patchOrderStatus(orderId, userPassport, orderStatus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);
				verify(orderWriter, never()).patchOrderStatus(any(), any(), any());
			});
		}
	}

	@Nested
	@DisplayName("영수증_별_주문_목록 조회")
	class getReceiptOrders {
		private Long receiptId = ReceiptInfoFixture.NON_ADJUSTMENT_RECEIPT_INFO().getReceiptId();
		private UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

		@Test
		void 영수증_별_주문_목록_조회_성공() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			List<Order> orders = List.of(OrderFixture.GENERAL_ORDER());

			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));
			BDDMockito.given(orderReader.getReceiptOrders(receiptId))
				.willReturn(orders);

			// when
			orderService.getReceiptOrders(receiptId, userPassport);

			// then
			verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
			verify(receiptValidator).validateAccessToReceipt(receipt, userPassport);
			verify(orderReader).getReceiptOrders(receiptId);
		}

		@Test
		void 영수증_조회_실패() {
			// given
			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.empty());

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.getReceiptOrders(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
				verify(receiptValidator, never()).validateAccessToReceipt(any(), any());
				verify(orderReader, never()).getReceiptOrders(any());
			});
		}

		@Test
		void 영수증_접근_권한_실패() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			BDDMockito.given(receiptReader.getReceiptWithCustomerAndOwner(receiptId))
				.willReturn(Optional.of(receipt));

			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(receiptValidator).validateAccessToReceipt(receipt, userPassport);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.getReceiptOrders(receiptId, userPassport))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(receiptReader).getReceiptWithCustomerAndOwner(receiptId);
				verify(receiptValidator).validateAccessToReceipt(receipt, userPassport);
				verify(orderReader, never()).getReceiptOrders(any());
			});
		}

		@Nested
		@DisplayName("주문_상세_조회")
		class getOrder {
			private Long orderId = OrderFixture.GENERAL_ORDER_ID;
			private UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			@Test
			void 주문_상세_조회_성공() {
				// given
				Order order = OrderFixture.GENERAL_ORDER();

				BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
					.willReturn(Optional.of(order));

				// when
				orderService.getOrder(orderId, userPassport);

				// then
				verify(orderReader).getOrderWithCustomerAndOwner(orderId);
				verify(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);
			}

			@Test
			void 주문_조회_실패() {
				// given
				BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
					.willReturn(Optional.empty());

				// when, then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(() -> orderService.getOrder(orderId, userPassport))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

					verify(orderReader).getOrderWithCustomerAndOwner(orderId);
					verify(receiptValidator, never()).validateAccessToReceipt(any(), any());
				});
			}

			@Test
			void 주문_접근_권한_실패() {
				// given
				Order order = OrderFixture.GENERAL_ORDER();
				BDDMockito.given(orderReader.getOrderWithCustomerAndOwner(orderId))
					.willReturn(Optional.of(order));

				doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
					.when(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);

				// when -> then
				assertSoftly(softly -> {
					softly.assertThatThrownBy(() -> orderService.getOrder(orderId, userPassport))
						.isInstanceOf(ServiceException.class)
						.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

					verify(orderReader).getOrderWithCustomerAndOwner(orderId);
					verify(receiptValidator).validateAccessToReceipt(order.getReceipt(), userPassport);
				});
			}
		}
	}
}
