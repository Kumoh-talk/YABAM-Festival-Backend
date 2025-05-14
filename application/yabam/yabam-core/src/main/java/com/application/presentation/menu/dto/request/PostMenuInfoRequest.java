package com.application.presentation.menu.dto.request;

import domain.pos.menu.entity.MenuInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메뉴 생성 요청 DTO")
public record PostMenuInfoRequest(

	@Schema(description = "메뉴 카테고리 Id", example = "1")
	@NotNull(message = "카테고리 Id는 필수입니다.")
	Long menuCategoryId,

	@Schema(description = "메뉴 이름", example = "불고기")
	@NotBlank(message = "메뉴 이름은 필수입니다.")
	String menuName,

	@Schema(description = "메뉴 가격", example = "10000")
	@NotNull(message = "메뉴 가격은 필수입니다.")
	Integer menuPrice,

	@Schema(description = "메뉴 설명", example = "달콤한 불고기")
	@NotBlank(message = "메뉴 설명은 필수입니다.")
	String menuDescription,
	@Schema(description = "메뉴 대표 이미지 URL", example = "https://example.com/image.jpg")
	@NotBlank(message = "메뉴 대표 이미지는 필수입니다.")
	String menuImageUrl,

	@Schema(description = "메뉴 품절 여부", example = "true")
	@NotNull(message = "메뉴 품절 여부는 필수입니다.")
	Boolean menuIsSoldOut,

	@Schema(description = "메뉴 추천 여부", example = "true")
	@NotNull(message = "메뉴 추천 여부는 필수입니다.")
	Boolean menuIsRecommended
) {
	public MenuInfo toMenuInfo() {
		return MenuInfo.builder()
			.id(null)
			.name(menuName)
			.price(menuPrice)
			.description(menuDescription)
			.imageUrl(menuImageUrl)
			.isSoldOut(menuIsSoldOut)
			.isRecommended(menuIsRecommended)
			.build();
	}
}
