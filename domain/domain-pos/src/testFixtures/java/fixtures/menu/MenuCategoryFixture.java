package fixtures.menu;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;
import fixtures.store.StoreFixture;

public class MenuCategoryFixture {

	public static final MenuCategoryInfo GENERAL_MENU_CATEGORY_INFO = MenuCategoryInfoFixture.GENERAL_MENU_CATEGORY_INFO();
	public static final MenuCategoryInfo DIFF_MENU_CATEGORY_INFO = MenuCategoryInfoFixture.DIFF_MENU_CATEGORY_INFO();

	public static final Store GENERAL_STORE = StoreFixture.GENERAL_CLOSE_STORE();

	public static MenuCategory GENERAL_MENU_CATEGORY() {
		return MenuCategory.of(GENERAL_MENU_CATEGORY_INFO, GENERAL_STORE);
	}

	public static MenuCategory DIFF_MENU_CATEGORY() {
		return MenuCategory.of(DIFF_MENU_CATEGORY_INFO, GENERAL_STORE);
	}

	public static MenuCategory CUSTOM_MENU_CATEGORY(MenuCategoryInfo menuCategoryInfo, Store store) {
		return MenuCategory.of(menuCategoryInfo, store);
	}
}
