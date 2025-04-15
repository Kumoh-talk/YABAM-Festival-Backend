package fixtures.menu;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;
import fixtures.store.StoreFixture;

public class MenuCategoryFixture {

	public static final MenuCategoryInfo GENERAL_MENU_CATEGORY_INFO = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();
	public static final Store GENERAL_STORE = StoreFixture.GENERAL_CLOSE_STORE();

	public static MenuCategory GENERAL_MENU_CATEGORY() {
		return MenuCategory.builder()
			.menuCategoryInfo(GENERAL_MENU_CATEGORY_INFO)
			.store(GENERAL_STORE)
			.build();
	}

	public static MenuCategory CUSTOM_MENU_CATEGORY(MenuCategoryInfo menuCategoryInfo, Store store) {
		return MenuCategory.builder()
			.menuCategoryInfo(menuCategoryInfo)
			.store(store)
			.build();
	}
}
