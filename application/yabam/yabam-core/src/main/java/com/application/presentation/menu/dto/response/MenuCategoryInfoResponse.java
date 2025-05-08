package com.application.presentation.menu.dto.response;

import domain.pos.menu.entity.MenuCategoryInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 카테고리 응답 DTO")
public record MenuCategoryInfoResponse(

	@Schema(description = "메뉴 카테고리 id", example = "1")
	Long menuCategoryId,
	@Schema(description = "메뉴 카테고리 이름", example = "음식류")
	String menuCategoryName,

	@Schema(description = "메뉴 카테고리 순서", example = "1")
	Integer menuCategoryOrder
) {
	public static MenuCategoryInfoResponse from(MenuCategoryInfo menuCategoryInfo) {
		return new MenuCategoryInfoResponse(
			menuCategoryInfo.getId(),
			menuCategoryInfo.getName(),
			menuCategoryInfo.getOrder());
	}
}
