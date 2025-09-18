package fixtures.menu.v2;

import static fixtures.menu.v2.ValidMenuState.*;
import static fixtures.menu.v2.state.MenuInfoStateFixture.*;

import java.time.LocalDateTime;

import com.vo.AuditStamp;

import domain.pos.menu.entity.v2.domain.Menu;

public class MenuFixture {

	public static Menu VALID_MENU() {
		return Menu.builder()
			.id(VALID_MENU_ID_1)
			.menuInfo(VALID_STATE())
			.order(VALID_ORDER_1)
			.isSoldOut(false)
			.isRecommended(false)
			.auditStamp(AuditStamp.create(LocalDateTime.now()))
			.storeId(VALID_STORE_ID_1)
			.menuCategoryId(VALID_MENU_CATEGORY_ID_1)
			.build();
	}
}
