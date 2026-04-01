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

	domain.pos.menu.entity.v2.domain.Menu create(domain.pos.menu.entity.v2.domain.Menu menu);

	Optional<domain.pos.menu.entity.v2.domain.Menu> readMenu(Long storeId, Long menuId);

	Slice<domain.pos.menu.entity.v2.domain.Menu> readMenuSlice(int pageSize,
		domain.pos.menu.entity.v2.domain.Menu menu);

	List<domain.pos.menu.entity.v2.domain.Menu> readCategoryMenuList(Long menuCategoryId);

	Integer readMaxOrder(Long menuCategoryId);

	domain.pos.menu.entity.v2.domain.Menu updateMenuInfo(domain.pos.menu.entity.v2.domain.Menu menu);

	void updateToTemporaryOrder(Long menuId, Integer temporaryOrder);

	Optional<domain.pos.menu.entity.v2.domain.Menu> updateOrder(Long userId, Long storeId, Long menuId,
		Integer updateOrder);

	domain.pos.menu.entity.v2.domain.Menu updateState(domain.pos.menu.entity.v2.domain.Menu menu);

	void delete(Long menuId);

	domain.pos.menu.entity.v2.domain.Menu refresh(Long menuId);

	void decrementOrdersInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void incrementOrdersInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

}
