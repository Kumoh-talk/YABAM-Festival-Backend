package com.application.presentation.menu.dto.request;

import domain.pos.menu.entity.MenuCategoryInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "메뉴 카테고리 수정 요청 DTO")
public record PatchMenuCategoryInfoRequest(
	@Schema(description = "메뉴 카테고리 이름", example = "음식류")
	@NotBlank(message = "메뉴 카테고리 이름은 필수입니다.")
	String menuCategoryName
) {
	public MenuCategoryInfo toMenuCategoryInfo(Long menuCategoryId) {
		return MenuCategoryInfo.of(menuCategoryId, menuCategoryName, null);
	}
}
