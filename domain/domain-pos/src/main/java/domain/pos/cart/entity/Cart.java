package domain.pos.cart.entity;

import java.util.List;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Cart {
	private UUID receiptId;
	private List<CartMenu> cartMenus;

	private Cart(UUID receiptId, List<CartMenu> cartMenus) {
		this.receiptId = receiptId;
		this.cartMenus = cartMenus;
	}

	public static Cart of(UUID receiptId, List<CartMenu> cartMenus) {
		return new Cart(receiptId, cartMenus);
	}
}
