package domain.pos.order.event;

import domain.pos.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderMenuStatusChangedEvent {
	private final Order order;

	private OrderMenuStatusChangedEvent(Order order) {
		this.order = order;
	}

	public static OrderMenuStatusChangedEvent from(Order order) {
		return new OrderMenuStatusChangedEvent(order);
	}
}
