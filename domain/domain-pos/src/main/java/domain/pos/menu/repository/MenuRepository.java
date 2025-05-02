package domain.pos.menu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;

public interface MenuRepository {
	Menu postMenu(Store store, MenuCategoryInfo menuCategory, MenuInfo menuInfo);

	Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId);

	List<Menu> getAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<MenuInfo> getMenuSlice(Pageable pageable, MenuInfo lastMenuInfo, Long menuCategoryId);

	boolean existsMenuOrder(Long menuCategoryId, Integer menuOrder);

	MenuInfo patchMenu(MenuInfo patchMenuInfo);

	MenuInfo patchMenuOrder(Menu menu, Integer patchOrder);

	void deleteMenu(Menu menu);

	Optional<MenuInfo> getMenuInfoById(Long menuId);
}
