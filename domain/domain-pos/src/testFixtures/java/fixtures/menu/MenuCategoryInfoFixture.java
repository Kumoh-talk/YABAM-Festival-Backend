package fixtures.menu;

import domain.pos.menu.entity.MenuCategoryInfo;

public class MenuCategoryInfoFixture {
	public static final Long GENERAL_MENU_CATEGORY_ID = 1L;
	public static final String GENERAL_MENU_CATEGORY_NAME = "categoryName";
	public static final int GENERAL_MENU_CATEGORY_ORDER = 1;

	public static MenuCategoryInfo GENERAL_MENU_CATEGORY_INFO() {
		return MenuCategoryInfo.builder()
			.menuCategoryId(GENERAL_MENU_CATEGORY_ID)
			.menuCategoryName(GENERAL_MENU_CATEGORY_NAME)
			.menuCategoryOrder(GENERAL_MENU_CATEGORY_ORDER)
			.build();
	}
}
