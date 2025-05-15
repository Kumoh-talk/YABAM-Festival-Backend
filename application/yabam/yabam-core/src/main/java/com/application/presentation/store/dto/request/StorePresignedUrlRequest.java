package com.application.presentation.store.dto.request;

import com.application.global.validator.ValidEnum;

import domain.pos.store.entity.vo.StoreImageProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "StorePresignedUrlRequest", description = "스토어 프리사인드 URL 요청")
public record StorePresignedUrlRequest(
	@Schema(description = "스토어 ID", example = "1")
	@NotNull
	@Min(value = 1, message = "스토어 ID는 1 이상이어야 합니다.")
	Long storeId,
	@Schema(description = "스토어 이미지 속성", example = "STORE_HEAD")
	@ValidEnum(message = "스토어 이미지 속성은 STORE_HEAD, STORE_DETAIL 중 하나여야 합니다.", enumClass = StoreImageProperty.class)
	StoreImageProperty storeImageProperty
) {
}
