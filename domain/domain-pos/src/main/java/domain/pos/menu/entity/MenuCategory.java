package domain.pos.menu.entity;

import domain.pos.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuCategory {
	private MenuCategoryInfo menuCategoryInfo;
	private Store store;

	@Builder
	public MenuCategory(MenuCategoryInfo menuCategoryInfo, Store store) {
		this.menuCategoryInfo = menuCategoryInfo;
		this.store = store;
	}
}
