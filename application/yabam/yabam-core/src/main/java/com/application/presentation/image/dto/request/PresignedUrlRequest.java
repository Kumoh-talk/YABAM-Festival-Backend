package com.application.presentation.image.dto.request;

import com.application.global.validator.ValidEnum;

import domain.pos.image.entity.ImageProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "PresignedUrlRequest", description = "스토어 프리사인드 URL 요청")
public record PresignedUrlRequest(
	@Schema(description = "스토어 ID", example = "1")
	@NotNull
	@Min(value = 1, message = "스토어 ID는 1 이상이어야 합니다.")
	Long storeId,
	@Schema(description = "스토어 이미지 속성", example = "STORE_HEAD")
	@ValidEnum(message = "스토어 이미지 속성은 STORE_HEAD, STORE_DETAIL, MENU_IMAGE 중 하나여야 합니다.",
		enumClass = ImageProperty.class)
	ImageProperty imageProperty
) {
}
