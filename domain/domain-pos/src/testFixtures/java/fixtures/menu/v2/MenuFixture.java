package fixtures.menu.v2;

import static fixtures.menu.v2.ValidMenuState.*;
import static fixtures.menu.v2.state.MenuInfoStateFixture.*;

import java.time.LocalDateTime;

import com.vo.AuditStamp;

import domain.pos.menu.entity.v2.domain.Menu;

public class MenuFixture {

	public static Menu VALID_MENU() {
		return Menu.fromInfra(VALID_MENU_ID_1, VALID_STATE(), VALID_ORDER_1, false,
			false, AuditStamp.create(LocalDateTime.now()), VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1);
	}
}
