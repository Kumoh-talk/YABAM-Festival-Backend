package fixtures.menu.v2;

import domain.pos.menu.entity.v2.domain.MenuCategory;

public class MenuCategoryFixture {
	public static MenuCategory VALID_CATEGORY() {
		return MenuCategory.fromInfra(1L, "음료", 1, 1L);
	}
}
