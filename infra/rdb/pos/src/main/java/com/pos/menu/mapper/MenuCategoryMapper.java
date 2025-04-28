package com.pos.menu.mapper;

import com.pos.menu.entity.MenuCategoryEntity;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;

public class MenuCategoryMapper {

	public static MenuCategoryEntity toMenuCategoryEntity(MenuCategoryInfo menuCategoryInfo, Store store) {
		return MenuCategoryEntity.of(menuCategoryInfo, store);
	}

	public static MenuCategory toMenuCategory(MenuCategoryEntity menuCategoryEntity, Store store) {
		return MenuCategory.of(toMenuCategoryInfo(menuCategoryEntity), store);
	}

	public static MenuCategoryInfo toMenuCategoryInfo(MenuCategoryEntity menuCategoryEntity) {
		return MenuCategoryInfo.of(menuCategoryEntity.getId(), menuCategoryEntity.getName(),
			menuCategoryEntity.getOrder());
	}
}
