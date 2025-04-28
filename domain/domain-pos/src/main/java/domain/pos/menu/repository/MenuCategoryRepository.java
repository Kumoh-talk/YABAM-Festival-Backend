package domain.pos.menu.repository;

import java.util.List;
import java.util.Optional;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;

public interface MenuCategoryRepository {
	MenuCategory postMenuCategory(Store store, MenuCategoryInfo menuCategoryInfo);

	Optional<MenuCategoryInfo> getMenuCategoryInfo(Long storeId, Long categoryId);

	List<MenuCategoryInfo> getAllByStoreIdWithLock(Long storeId);

	List<MenuCategoryInfo> getMenuCategoryInfoList(Long storeId);

	boolean existsMenuCategory(Long storeId, Long menuCategoryId);

	boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder);

	MenuCategoryInfo patchMenuCategory(MenuCategoryInfo patchMenuCategoryInfo);

	MenuCategoryInfo patchMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo, Integer patchOrder);

	void deleteMenuCategory(Long storeId, Long categoryId);

}
