package com.application.presentation.menu.dto.request;

import domain.pos.menu.entity.MenuCategoryInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메뉴 카테고리 생성 요청 DTO")
public record PostMenuCategoryInfoRequest(

	@Schema(description = "메뉴 카테고리 이름", example = "음식류")
	@NotBlank(message = "메뉴 카테고리 이름은 필수입니다.")
	String menuCategoryName,

	@Schema(description = "메뉴 카테고리 순서", example = "1")
	@NotNull(message = "메뉴 카테고리 순서는 필수입니다.")
	Integer menuCategoryOrder
) {
	public MenuCategoryInfo toMenuCategoryInfo() {
		return MenuCategoryInfo.of(null, menuCategoryName, menuCategoryOrder);
	}
}
