package domain.pos.menu.entity;

import domain.pos.store.entity.Store;
import lombok.Getter;

@Getter
public class MenuCategory {
	private MenuCategoryInfo menuCategoryInfo;
	private Store store;

	private MenuCategory(MenuCategoryInfo menuCategoryInfo, Store store) {
		this.menuCategoryInfo = menuCategoryInfo;
		this.store = store;
	}

	private MenuCategory(MenuCategoryInfo menuCategoryInfo) {
		this.menuCategoryInfo = menuCategoryInfo;
		this.store = null;
	}

	public static MenuCategory of(MenuCategoryInfo menuCategoryInfo, Store store) {
		return new MenuCategory(menuCategoryInfo, store);
	}

	public static MenuCategory fromWithoutStore(MenuCategoryInfo menuCategoryInfo) {
		return new MenuCategory(menuCategoryInfo);
	}
}
