package com.application.presentation.menu.dto.response;

import domain.pos.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 무한스크롤 응답 DTO")
public record MenuSliceResponse(

	@Schema(description = "메뉴 세부정보")
	MenuInfoResponse menuInfo,
	@Schema(description = "메뉴 카테고리 세부정보")
	MenuCategoryInfoResponse menuCategoryInfo
) {
	public static MenuSliceResponse from(Menu menu) {
		return new MenuSliceResponse(
			MenuInfoResponse.from(menu.getMenuInfo()),
			MenuCategoryInfoResponse.from(menu.getMenuCategory().getMenuCategoryInfo()));
	}

}
