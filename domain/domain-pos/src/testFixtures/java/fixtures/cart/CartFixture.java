package fixtures.cart;

import java.util.List;
import java.util.UUID;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.entity.CartMenu;
import fixtures.menu.MenuInfoFixture;

public class CartFixture {

	private static final UUID RECEIPT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

	private static final List<CartMenu> SINGLE_CART_MENUS = List.of(
		CartMenu.of(1, MenuInfoFixture.GENERAL_MENU_INFO())
	);

	private static final List<CartMenu> TWO_CART_MENUS = List.of(
		CartMenu.of(1, MenuInfoFixture.GENERAL_MENU_INFO()),
		CartMenu.of(4, MenuInfoFixture.DIFF_MENU_INFO())
	);

	public static Cart GENERAL_CART_SINGLE() {
		return Cart.of(RECEIPT_ID, SINGLE_CART_MENUS);
	}

	public static Cart GENERAL_CART_TWO() {
		return Cart.of(RECEIPT_ID, TWO_CART_MENUS);
	}
}
