package domain.pos.menu.port.provided;

import java.util.List;

import domain.pos.menu.entity.v2.domain.MenuCategory;

public interface MenuCategoryRead {
	List<MenuCategory> readMenuCategoryList(Long storeId);
}
