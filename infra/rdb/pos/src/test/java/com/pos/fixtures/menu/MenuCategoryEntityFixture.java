package com.pos.fixtures.menu;

import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.mapper.MenuCategoryMapper;

import domain.pos.menu.entity.MenuCategory;

public class MenuCategoryEntityFixture {
	public static MenuCategoryEntity CUSTOM_MENU_CATEGORY_ENTITY(MenuCategory menuCategory) {
		return MenuCategoryMapper.toMenuCategoryEntity(menuCategory.getMenuCategoryInfo(), menuCategory.getStore());
	}
}
