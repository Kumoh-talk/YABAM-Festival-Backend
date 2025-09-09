package domain.pos.menu.port.required;

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

	Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId, Long lastMenuCategoryId);

	List<Menu> getAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<Menu> getMenuSlice(int pageSize, Long storeId, MenuInfo lastMenuInfo,
		MenuCategoryInfo lastMenuCategoryInfo);

	List<MenuInfo> getCategoryMenuList(Long storeId, Long menuCategoryId);

	boolean existsMenu(Long storeId, Long menuId);

	boolean existsMenuOrder(Long menuCategoryId, Integer menuOrder);

	MenuInfo patchMenuInfo(MenuInfo patchMenuInfo);

	MenuInfo patchMenuOrder(Menu menu, Integer patchOrder);

	void deleteMenu(Menu menu);

	Long countByIdIn(Long storeId, Set<Long> menuIds);

	Optional<MenuInfo> getMenuInfoById(Long menuId);

	// v2
	domain.pos.menu.entity.v2.Menu createMenu(domain.pos.menu.entity.v2.Menu menu);

	Optional<domain.pos.menu.entity.v2.Menu> readMenu(Long storeId, Long menuId);

	Integer readMaxMenuOrder(Long menuCategoryId);

	domain.pos.menu.entity.v2.Menu updateMenuInfo(domain.pos.menu.entity.v2.Menu menu);

	domain.pos.menu.entity.v2.Menu updateOrder(Long menuId, Integer updateOrder);

	void updateTemporaryOrder(Long menuId, Integer temporaryOrder);

	domain.pos.menu.entity.v2.Menu updateState(domain.pos.menu.entity.v2.Menu menu);

	void deleteMenu(Long menuId);

	domain.pos.menu.entity.v2.Menu refrsh(domain.pos.menu.entity.v2.Menu menu);

	void decrementMenuOrdersInRange(Long menuCategoryId, Integer startOrder, Integer finishOrder);

	void incrementMenuOrdersInRange(Long menuCategoryId, Integer startOrder, Integer finishOrder);
}
