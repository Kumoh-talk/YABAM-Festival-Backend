package fixtures.menu;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;

public class MenuFixture {
	public static Menu CUSTOM_MENU(MenuInfo menuInfo, Store store, MenuCategory menuCategory) {
		return Menu.builder()
			.menuInfo(menuInfo)
			.store(store)
			.menuCategory(menuCategory)
			.build();
	}
}
