package com.application.presentation.menu.dto.response;

import com.application.presentation.store.dto.response.StoreInfoResponse;

import domain.pos.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 응답 DTO")
public record MenuResponse(
	@Schema(description = "메뉴 세부정보")
	MenuInfoResponse menuInfo,
	@Schema(description = "가게 세부정보")
	StoreInfoResponse storeInfo,
	@Schema(description = "메뉴 카테고리 세부정보")
	MenuCategoryInfoResponse menuCategoryInfo
) {
	public static MenuResponse from(Menu menu) {
		return new MenuResponse(
			MenuInfoResponse.from(menu.getMenuInfo()),
			StoreInfoResponse.of(menu.getStore()),
			MenuCategoryInfoResponse.from(menu.getMenuCategory().getMenuCategoryInfo()));
	}

}
