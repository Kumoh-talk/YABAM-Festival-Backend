package domain.pos.order.entity;

import java.util.List;

import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.receipt.entity.Receipt;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Order {
	private Long orderId;
	private OrderStatus orderStatus;
	private Integer totalPrice;
	private Receipt receipt;
	private List<OrderMenu> orderMenus;

	@Builder
	public Order(Long orderId, OrderStatus orderStatus, Integer totalPrice, Receipt receipt,
		List<OrderMenu> orderMenus) {
		this.orderId = orderId;
		this.orderStatus = orderStatus;
		this.totalPrice = totalPrice;
		this.receipt = receipt;
		this.orderMenus = orderMenus;
	}

	private Order(Long orderId) {
		this.orderId = orderId;
	}

	public static Order from(Long orderId) {
		return new Order(orderId);
	}
}
