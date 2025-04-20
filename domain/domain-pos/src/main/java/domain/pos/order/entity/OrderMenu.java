package domain.pos.order.entity;

import domain.pos.menu.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderMenu {
	private Long orderMenuId;
	private Integer quantity;
	private Order order;
	private Menu menu;

	@Builder
	public OrderMenu(Long orderMenuId, Integer quantity, Order order, Menu menu) {
		this.orderMenuId = orderMenuId;
		this.quantity = quantity;
		this.order = order;
		this.menu = menu;
	}
}
