package fixtures.order;

import domain.pos.menu.entity.Menu;
import domain.pos.order.entity.OrderMenu;
import fixtures.menu.MenuFixture;

public class OrderMenuFixture {
	public static final Long GENERAL_ORDER_MENU_ID = 1L;
	public static final int GENERAL_ORDER_MENU_QUANTITY = 1;
	public static final Menu GENERAL_ORDER_MENU = MenuFixture.GENERAL_MENU();

	public static OrderMenu GENERAL_ORDER_MENU() {
		return OrderMenu.builder()
			.orderMenuId(GENERAL_ORDER_MENU_ID)
			.quantity(GENERAL_ORDER_MENU_QUANTITY)
			.menu(GENERAL_ORDER_MENU)
			.build();
	}
}
