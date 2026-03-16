package com.application.presentation.menu.dto.response;

import domain.pos.menu.entity.MenuInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "메뉴 세부정보 응답 DTO")
public record MenuInfoResponse(

	@Schema(description = "메뉴 id", example = "1")
	Long menuId,

	@Schema(description = "메뉴 순서", example = "1")
	Integer menuOrder,
	@Schema(description = "메뉴 이름", example = "불고기")
	String menuName,

	@Schema(description = "메뉴 가격", example = "10000")
	Integer menuPrice,

	@Schema(description = "메뉴 설명", example = "달콤한 불고기")
	String menuDescription,
	@Schema(description = "메뉴 대표 이미지 URL", example = "https://example.com/image.jpg")
	String menuImageUrl,

	@Schema(description = "메뉴 품절 여부", example = "true")
	Boolean menuIsSoldOut,

	@Schema(description = "메뉴 추천 여부", example = "true")
	Boolean menuIsRecommended
) {
	public static MenuInfoResponse from(MenuInfo menuInfo) {
		return MenuInfoResponse.builder()
			.menuId(menuInfo.getId())
			.menuOrder(menuInfo.getOrder())
			.menuName(menuInfo.getName())
			.menuPrice(menuInfo.getPrice())
			.menuDescription(menuInfo.getDescription())
			.menuImageUrl(menuInfo.getImageUrl())
			.menuIsSoldOut(menuInfo.isSoldOut())
			.menuIsRecommended(menuInfo.isRecommended())
			.build();
	}
}
