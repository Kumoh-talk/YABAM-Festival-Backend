package domain.pos.menu.port.required;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;

@Repository
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

	void lock(Long menuCategoryId);

	Long readVersion(Long menuCategoryId);

	void bumpVersion(Long menuCategoryId);

	Optional<domain.pos.menu.entity.v2.domain.MenuCategory> create(Long userId, Long storeId,
		domain.pos.menu.entity.v2.domain.MenuCategory menuCategory);

	List<domain.pos.menu.entity.v2.domain.MenuCategory> readList(Long storeId);

	Integer readMaxOrder(Long storeId);

	Optional<domain.pos.menu.entity.v2.domain.MenuCategory> readMenuCategory(Long storeId, Long menuCategoryId);

	Optional<domain.pos.menu.entity.v2.domain.MenuCategory> updateName(Long userId, Long storeId,
		domain.pos.menu.entity.v2.domain.MenuCategory menuCategory);

	void updateToTemporaryOrder(Long menuCategoryId, Integer temporaryOrder);

	Optional<domain.pos.menu.entity.v2.domain.MenuCategory> updateOrder(Long userId, Long storeId, Long menuCategoryId,
		Integer updateOrder);

	Optional<Object> delete(Long userId, Long storeId, Long menuCategoryId);

	void incrementOrdersInRange(Long storeId, Integer startOrder, Integer endOrder);

	void decrementOrdersInRange(Long storeId, Integer startOrder, Integer endOrder);

	domain.pos.menu.entity.v2.domain.MenuCategory refresh(Long menuCategoryId);
}
