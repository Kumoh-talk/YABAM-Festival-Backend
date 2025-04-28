package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;

import com.pos.menu.entity.MenuCategoryEntity;

public interface MenuCategoryQueryDslRepository {
	Optional<MenuCategoryEntity> findByIdAndStoreId(Long menuCategoryId, Long storeId);

	List<MenuCategoryEntity> findAllByStoreId(Long storeId);

	List<MenuCategoryEntity> findAllByStoreIdWithLock(Long storeId);

	boolean existsByIdAndStoreId(Long menuCategoryId, Long storeId);

	boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder);

	void decreaseOrderInRange(Long storeId, Integer startOrder, Integer endOrder);

	void increaseOrderInRange(Long storeId, Integer startOrder, Integer endOrder);

	void decreaseOrderWhereGT(Long storeId, Integer deleteOrder);
}
