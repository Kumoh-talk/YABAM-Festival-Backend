package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Slice;

import com.pos.menu.entity.MenuEntity;

public interface MenuQueryDslRepository {
	Optional<MenuEntity> findByIdAndStoreId(Long menuId, Long storeId);

	List<MenuEntity> findAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<MenuEntity> findSliceByMenuCategoryId(int pageSize, Integer lastMenuOrder, Long menuCategoryId);

	Optional<Integer> findMaxOrderByMenuCategoryId(Long storeId);

	boolean existsByIdAndStoreId(Long menuId, Long storeId);

	boolean existsMenuOrder(Long menuCategoryId, int menuOrder);

	void decreaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void increaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void decreaseOrderWhereGT(Long menuCategoryId, Integer deleteOrder);

	Long countByIdIn(Long storeId, Set<Long> menuIds);

}
