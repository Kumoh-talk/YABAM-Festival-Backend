package fixtures.order;

import java.util.List;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.receipt.entity.Receipt;
import fixtures.receipt.ReceiptFixture;

public class OrderFixture {
	public static final Long GENERAL_ORDER_ID = 1L;
	public static final OrderStatus GENERAL_ORDER_STATUS = OrderStatus.ORDERED;
	public static final int GENERAL_TOTAL_PRICE = 10000;
	public static final Receipt GENERAL_RECEIPT = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
	public static final List<OrderMenu> GENERAL_ORDER_MENUS = List.of(OrderMenuFixture.GENERAL_ORDER_MENU());

	public static Order GENERAL_ORDER() {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(GENERAL_ORDER_STATUS)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}

	public static Order ORDERED_ORDER() {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(OrderStatus.ORDERED)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}

	public static Order RECEIVED_ORDER() {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(OrderStatus.RECEIVED)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}

	public static Order CANCELLED_ORDER() {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(OrderStatus.CANCELED)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}

	public static Order COMPLETED_ORDER() {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(OrderStatus.COMPLETED)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}

	public static Order CREATE_ORDER_WITH_STATUS(OrderStatus status) {
		return Order.builder()
			.orderId(GENERAL_ORDER_ID)
			.orderStatus(status)
			.totalPrice(GENERAL_TOTAL_PRICE)
			.receipt(GENERAL_RECEIPT)
			.orderMenus(GENERAL_ORDER_MENUS)
			.build();
	}
}
