package domain.pos.menu.entity;

import domain.pos.store.entity.Store;
import lombok.Getter;

@Getter
public class Menu {
	private MenuInfo menuInfo;
	private Store store;
	private MenuCategory menuCategory;

	private Menu(MenuInfo menuInfo, Store store, MenuCategory menuCategory) {
		this.menuInfo = menuInfo;
		this.store = store;
		this.menuCategory = menuCategory;
	}

	public static Menu of(MenuInfo menuInfo, Store store, MenuCategory menuCategory) {
		return new Menu(menuInfo, store, menuCategory);
	}
}
