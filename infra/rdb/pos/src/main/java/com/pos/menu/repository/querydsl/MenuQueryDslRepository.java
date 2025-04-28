package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.pos.menu.entity.MenuEntity;

public interface MenuQueryDslRepository {
	Optional<MenuEntity> findByIdAndStoreId(Long menuId, Long storeId);

	List<MenuEntity> findAllByStoreIdWithCategoryAndLock(Long storeId);

	Slice<MenuEntity> findSliceByMenuCategoryId(Pageable pageable, Integer lastMenuOrder, Long menuCategoryId);

	boolean existsMenuOrder(Long menuCategoryId, int menuOrder);

	void decreaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void increaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder);

	void decreaseOrderWhereGT(Long menuCategoryId, Integer deleteOrder);

}
