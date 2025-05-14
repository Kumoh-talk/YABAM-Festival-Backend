package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Slice;

import com.pos.menu.entity.MenuEntity;

import domain.pos.menu.entity.MenuCategoryInfo;

public interface MenuQueryDslRepository {
	Optional<MenuEntity> findByIdAndStoreId(Long menuId, Long storeId);

	Optional<MenuEntity> findByIdAndStoreIdAndMenuCategoryId(Long menuId, Long storeId, Long menuCategoryId);

	List<MenuEntity> findAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<MenuEntity> findSliceByMenuCategoryId(int pageSize, Long storeId, Integer lastMenuOrder,
		MenuCategoryInfo lastMenuCategoryInfo);

	List<MenuEntity> findAllByStoreIdAndMenuCategoryId(Long storeId, Long menuCategoryId);

	Optional<Integer> findMaxOrderByMenuCategoryId(Long storeId);

	boolean existsByIdAndStoreId(Long menuId, Long storeId);

	boolean existsMenuOrder(Long menuCategoryId, int menuOrder);

	void decreaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void increaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void decreaseOrderWhereGT(Long menuCategoryId, Integer deleteOrder);

	Long countByIdIn(Long storeId, Set<Long> menuIds);

}
