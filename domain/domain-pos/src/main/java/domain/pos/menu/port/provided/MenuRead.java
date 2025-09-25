package domain.pos.menu.port.provided;

import java.util.List;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.entity.v2.dto.MenuSliceWithVersion;

public interface MenuRead {
	Menu readMenu(Long storeId, Long menuId);

	MenuSliceWithVersion readMenuSlice(Long storeId, int pageSize, Long lastMenuId, Long version);

	List<Menu> readCategoryMenuList(Long storeId, Long menuCategoryId);
}
