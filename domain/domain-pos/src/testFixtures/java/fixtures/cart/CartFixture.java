package fixtures.cart;

import java.util.List;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.entity.CartMenu;
import fixtures.menu.MenuInfoFixture;

public class CartFixture {
	public static final Long RECEIPT_ID = 1L;

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
