package fixtures.menu;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;
import fixtures.store.StoreFixture;

public class MenuFixture {
	public static final MenuInfo GENERAL_MENU_INFO = MenuInfoFixture.GENERAL_MENU_INFO();
	public static final Store GENERAL_STORE = StoreFixture.GENERAL_OPEN_STORE();
	public static final MenuCategory GENERAL_MENU_CATEGORY = MenuCategoryFixture.GENERAL_MENU_CATEGORY();

	public static Menu GENERAL_MENU() {
		return Menu.builder()
			.menuInfo(GENERAL_MENU_INFO)
			.store(GENERAL_STORE)
			.menuCategory(GENERAL_MENU_CATEGORY)
			.build();
	}

	public static Menu CUSTOM_MENU(MenuInfo menuInfo, Store store, MenuCategory menuCategory) {
		return Menu.builder()
			.menuInfo(menuInfo)
			.store(store)
			.menuCategory(menuCategory)
			.build();
	}
}
