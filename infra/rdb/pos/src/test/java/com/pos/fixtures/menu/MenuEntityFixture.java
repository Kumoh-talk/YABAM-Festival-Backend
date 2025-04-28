package com.pos.fixtures.menu;

import com.pos.menu.entity.MenuEntity;
import com.pos.menu.mapper.MenuMapper;

import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;

public class MenuEntityFixture {
	public static MenuEntity CUSTOM_MENU_ENTITY(MenuInfo menuInfo, Store store, MenuCategoryInfo menuCategoryInfo) {
		return MenuMapper.toMenuEntity(menuInfo, store, menuCategoryInfo);
	}
}
