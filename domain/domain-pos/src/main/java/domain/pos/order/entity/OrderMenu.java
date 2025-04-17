package domain.pos.order.entity;

import domain.pos.menu.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderMenu {
	private Long orderMenuId;
	private Integer quantity;
	private Menu menu;

	@Builder
	public OrderMenu(Long orderMenuId, Integer quantity, Menu menu) {
		this.orderMenuId = orderMenuId;
		this.quantity = quantity;
		this.menu = menu;
	}
}
