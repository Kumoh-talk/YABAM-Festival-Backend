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
import domain.pos.receipt.implement.ReceiptCustomerWriter;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.store.implement.SaleValidator;
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
	private SaleValidator saleValidator;
	@Mock
	private ReceiptCustomerWriter receiptCustomerWriter;

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
		void 회원_주문_등록_성공() {
			주문_등록_성공_공통_로직(userPassport);
			verify(receiptCustomerWriter).postReceiptCustomer(userPassport.getUserId(), receiptId);
		}

		@Test
		void 점주_주문_등록_성공() {
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();
			BDDMockito.given(receiptValidator.isStoreOwner(any(), any()))
				.willReturn(true);
			주문_등록_성공_공통_로직(userPassport);
			verify(receiptCustomerWriter, never()).postReceiptCustomer(userPassport.getUserId(), receiptId);
		}

		@Test
		void 비회원_주문_등록_성공() {
			UserPassport userPassport = UserFixture.ANONYMOUS_USER_PASSPORT();
			주문_등록_성공_공통_로직(userPassport);
			verify(receiptCustomerWriter, never()).postReceiptCustomer(userPassport.getUserId(), receiptId);
		}

		private void 주문_등록_성공_공통_로직(UserPassport userPassport) {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
			List<Long> orderMenuIds = orderMenus.stream()
				.map(orderMenu -> orderMenu.getMenu().getMenuInfo().getMenuId())
				.toList();

			BDDMockito.given(receiptReader.getNonAdjustReceiptWithStore(receiptId))
				.willReturn(Optional.of(receipt));
			BDDMockito.given(
					menuReader.getMenuInfo(
						BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
						ArgumentMatchers.argThat(orderMenuIds::contains)))
				.willReturn(Optional.of(MenuInfoFixture.GENERAL_MENU_INFO()));

			// when
			orderService.postOrder(receiptId, userPassport, orderMenus);

			// then
			verify(receiptReader).getNonAdjustReceiptWithStore(receiptId);
			verify(saleValidator).validateSaleOpen(receipt.getSale());
			verify(menuReader).getMenuInfo(
				BDDMockito.eq(receipt.getSale().getStore().getStoreId()),
				ArgumentMatchers.argThat(orderMenuIds::contains));
			verify(orderWriter).postOrder(receiptId, orderMenus);
		}

		@Test
		void 영수증_조회_실패() {
			// given
			BDDMockito.given(receiptReader.getNonAdjustReceiptWithStore(receiptId))
				.willReturn(Optional.empty());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.postOrder(receiptId, userPassport, orderMenus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptReader).getNonAdjustReceiptWithStore(receiptId);
				verify(saleValidator, never()).validateSaleOpen(any());
				verify(menuReader, never()).getMenuInfo(anyLong(), any());
				verify(receiptCustomerWriter, never()).postReceiptCustomer(userPassport.getUserId(), receiptId);
				verify(orderWriter, never()).postOrder(receiptId, orderMenus);
			});
		}

		@Test
		void 영업_종료로_인한_주문_실패() {
			// given
			Receipt receipt = ReceiptFixture.GENERAL_CLOSE_SALE_NON_ADJSTMENT_RECEIPT();
			BDDMockito.given(receiptReader.getNonAdjustReceiptWithStore(receiptId))
				.willReturn(Optional.of(receipt));
			doThrow(new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED))
				.when(saleValidator).validateSaleOpen(receipt.getSale());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.postOrder(receiptId, userPassport, orderMenus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_ACCESS_DENIED);

				verify(receiptReader).getNonAdjustReceiptWithStore(receiptId);
				verify(saleValidator).validateSaleOpen(receipt.getSale());
				verify(menuReader, never()).getMenuInfo(anyLong(), any());
				verify(receiptCustomerWriter, never()).postReceiptCustomer(userPassport.getUserId(), receiptId);
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

			BDDMockito.given(receiptReader.getNonAdjustReceiptWithStore(receiptId))
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

				verify(receiptReader).getNonAdjustReceiptWithStore(receiptId);
				verify(saleValidator).validateSaleOpen(receipt.getSale());
				verify(menuReader).getMenuInfo(anyLong(), any());
				verify(receiptCustomerWriter, never()).postReceiptCustomer(userPassport.getUserId(), receiptId);
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

			BDDMockito.given(orderReader.getOrderWithStore(orderId))
				.willReturn(Optional.of(order));

			// when
			orderService.patchOrderStatus(orderId, userPassport, orderStatus);

			// then
			verify(orderReader).getOrderWithStore(orderId);
			verify(receiptValidator).validateRole(order.getReceipt(), userPassport);
			verify(orderWriter).patchOrderStatus(BDDMockito.eq(order), BDDMockito.eq(orderStatus), any());
		}

		@Test
		void 주문_조회_실패() {
			// given
			BDDMockito.given(orderReader.getOrderWithStore(orderId))
				.willReturn(Optional.empty());

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.patchOrderStatus(orderId, userPassport, orderStatus))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

				verify(orderReader).getOrderWithStore(orderId);
				verify(receiptValidator, never()).validateRole(any(), any());
				verify(orderWriter, never()).patchOrderStatus(any(), any(), any());
			});
		}
	}

	@Nested
	@DisplayName("영수증_별_주문_목록 조회")
	class getReceiptOrders {
		private Long receiptId = ReceiptInfoFixture.NON_ADJUSTMENT_RECEIPT_INFO().getReceiptId();

		@Test
		void 영수증_별_주문_목록_조회_성공() {
			// when
			orderService.getReceiptOrders(receiptId);

			// then
			verify(receiptValidator).validateReceipt(receiptId);
			verify(orderReader).getReceiptOrders(receiptId);
		}

		@Test
		void 영수증_조회_실패() {
			// given
			doThrow(new ServiceException(ErrorCode.RECEIPT_NOT_FOUND))
				.when(receiptValidator).validateReceipt(receiptId);

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.getReceiptOrders(receiptId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);

				verify(receiptValidator).validateReceipt(receiptId);
				verify(orderReader, never()).getReceiptOrders(receiptId);
			});
		}
	}

	@Nested
	@DisplayName("주문_상세_조회")
	class getOrder {
		private Long orderId = OrderFixture.GENERAL_ORDER_ID;

		@Test
		void 주문_상세_조회_성공() {
			// given
			Order order = OrderFixture.GENERAL_ORDER();
			BDDMockito.given(orderReader.getOrder(orderId))
				.willReturn(Optional.of(order));

			// when
			orderService.getOrder(orderId);

			// then
			verify(orderReader).getOrder(orderId);
		}

		@Test
		void 주문_조회_실패() {
			// given
			BDDMockito.given(orderReader.getOrder(orderId))
				.willReturn(Optional.empty());

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderService.getOrder(orderId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

				verify(orderReader).getOrder(orderId);
			});
		}
	}
}
