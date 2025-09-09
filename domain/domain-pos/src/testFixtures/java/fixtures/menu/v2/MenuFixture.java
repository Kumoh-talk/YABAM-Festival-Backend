package fixtures.menu.v2;

import static fixtures.menu.v2.ValidMenuState.*;
import static fixtures.menu.v2.state.MenuInfoStateFixture.*;

import domain.pos.menu.entity.v2.Menu;

public class MenuFixture {

	public static Menu VALID_MENU() {
		return Menu.create(VALID_STATE(), VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1);
	}
}
