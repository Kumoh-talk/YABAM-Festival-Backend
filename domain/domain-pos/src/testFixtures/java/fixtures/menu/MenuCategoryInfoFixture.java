package fixtures.menu;

import domain.pos.menu.entity.MenuCategoryInfo;

public class MenuCategoryInfoFixture {
	public static final Long GENERAL_MENU_CATEGORY_ID = 1L;
	public static final Long DIFF_MENU_CATEGORY_ID = 2L;

	public static final String GENERAL_MENU_CATEGORY_NAME = "categoryName";
	public static final String DIFF_MENU_CATEGORY_NAME = "diffCategoryName";

	public static final Integer GENERAL_MENU_CATEGORY_ORDER = 1;
	public static final Integer DIFF_MENU_CATEGORY_ORDER = 2;

	public static MenuCategoryInfo GENERAL_MENU_CATEGORY_INFO() {
		return MenuCategoryInfo.of(GENERAL_MENU_CATEGORY_ID, GENERAL_MENU_CATEGORY_NAME, GENERAL_MENU_CATEGORY_ORDER);
	}

	public static MenuCategoryInfo DIFF_MENU_CATEGORY_INFO() {
		return MenuCategoryInfo.of(DIFF_MENU_CATEGORY_ID, DIFF_MENU_CATEGORY_NAME, DIFF_MENU_CATEGORY_ORDER);
	}

	public static MenuCategoryInfo PATCH_GENERAL_MENU_INFO() {
		return MenuCategoryInfo.of(GENERAL_MENU_CATEGORY_ID, DIFF_MENU_CATEGORY_NAME, GENERAL_MENU_CATEGORY_ORDER);
	}
}
