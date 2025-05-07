package domain.pos.order.entity;

import domain.pos.cart.entity.CartMenu;
import domain.pos.menu.entity.Menu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderMenu {
	private Long orderMenuId;
	private OrderMenuStatus orderMenuStatus;
	private Integer quantity;
	private Order order;
	private Menu menu;

	@Builder
	public OrderMenu(Long orderMenuId, OrderMenuStatus orderMenuStatus, Integer quantity, Order order, Menu menu) {
		this.orderMenuId = orderMenuId;
		this.orderMenuStatus = orderMenuStatus;
		this.quantity = quantity;
		this.order = order;
		this.menu = menu;
	}

	public static OrderMenu of(CartMenu cartMenu, Order order) {
		return OrderMenu.builder()
			.orderMenuStatus(null)
			.quantity(cartMenu.getQuantity())
			.order(order)
			.menu(Menu.of(cartMenu.getMenuInfo(), null, null))
			.build();
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public void setOrderMenuStatus(OrderMenuStatus orderMenuStatus) {
		this.orderMenuStatus = orderMenuStatus;
	}
}
