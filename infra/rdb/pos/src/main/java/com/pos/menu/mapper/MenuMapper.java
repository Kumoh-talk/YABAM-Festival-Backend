package com.pos.menu.mapper;

import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.entity.MenuEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.store.entity.Store;

public class MenuMapper {
	public static MenuEntity toMenuEntity(MenuInfo menuInfo, Store store, MenuCategoryInfo menuCategoryInfo) {
		return MenuEntity.of(menuInfo,
			store == null ? null : StoreMapper.toStoreEntity(store.getStoreId()),
			menuCategoryInfo == null ? null : MenuCategoryEntity.from(menuCategoryInfo.getId()));
	}

	public static Menu toMenu(MenuEntity menuEntity, Store store, MenuCategory menuCategory) {
		return Menu.of(toMenuInfo(menuEntity), store, menuCategory);
	}

	public static MenuInfo toMenuInfo(MenuEntity menuEntity) {
		return MenuInfo.builder()
			.id(menuEntity.getId())
			.order(menuEntity.getOrder())
			.name(menuEntity.getName())
			.price(menuEntity.getPrice())
			.description(menuEntity.getDescription())
			.imageUrl(menuEntity.getImageUrl())
			.isSoldOut(menuEntity.isSoldOut())
			.isRecommended(menuEntity.isRecommended())
			.build();
	}
}
