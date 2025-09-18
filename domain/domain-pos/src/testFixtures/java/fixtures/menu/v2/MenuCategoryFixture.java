package fixtures.menu.v2;

import domain.pos.menu.entity.v2.domain.MenuCategory;

public class MenuCategoryFixture {
	public static MenuCategory VALID_CATEGORY() {
		return MenuCategory.builder()
			.id(1L)
			.name("음료")
			.order(1)
			.storeId(1L)
			.build();
	}
}
