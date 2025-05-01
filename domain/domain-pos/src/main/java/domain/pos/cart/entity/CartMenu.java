package domain.pos.cart.entity;

import domain.pos.menu.entity.MenuInfo;
import lombok.Getter;

@Getter
public class CartMenu {
	private Integer quantity;
	private MenuInfo menuInfo;

	private CartMenu(Integer quantity, MenuInfo menuInfo) {
		this.quantity = quantity;
		this.menuInfo = menuInfo;
	}

	public static CartMenu of(Integer quantity, MenuInfo menuInfo) {
		return new CartMenu(quantity, menuInfo);
	}
}
