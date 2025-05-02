package domain.pos.cart.entity;

import java.util.List;

import lombok.Getter;

@Getter
public class Cart {
	private Long receiptId;
	private List<CartMenu> cartMenus;

	private Cart(Long receiptId, List<CartMenu> cartMenus) {
		this.receiptId = receiptId;
		this.cartMenus = cartMenus;
	}

	public static Cart of(Long receiptId, List<CartMenu> cartMenus) {
		return new Cart(receiptId, cartMenus);
	}
}
