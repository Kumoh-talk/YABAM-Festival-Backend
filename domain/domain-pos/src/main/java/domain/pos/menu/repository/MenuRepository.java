package domain.pos.menu.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;

@Repository
public interface MenuRepository {
	Menu postMenu(Store store, MenuCategoryInfo menuCategory, MenuInfo menuInfo);

	Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId);

	List<Menu> getAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<MenuInfo> getMenuSlice(int pageSize, MenuInfo lastMenuInfo, Long menuCategoryId);

	boolean existsMenu(Long storeId, Long menuId);

	boolean existsMenuOrder(Long menuCategoryId, Integer menuOrder);

	MenuInfo patchMenuInfo(MenuInfo patchMenuInfo);

	MenuInfo patchMenuOrder(Menu menu, Integer patchOrder);

	void deleteMenu(Menu menu);

	Long countByIdIn(Long storeId, Set<Long> menuIds);

	Optional<MenuInfo> getMenuInfoById(Long menuId);
}
