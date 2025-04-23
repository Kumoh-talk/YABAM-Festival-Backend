package com.application.presentation.store.dto.request;

import java.awt.geom.Point2D;

import domain.pos.store.entity.StoreInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 생성 요청 DTO")
public record StoreWriteRequest(
	@Schema(description = "가게 이름", example = "가게 이름")
	@NotBlank(message = "가게 이름은 필수입니다.")
	String storeName,
	@Schema(description = "가게 위도", example = "37.123456")
	@NotNull(message = "위도는 필수입니다.")
	Double latitude,
	@Schema(description = "가게 경도", example = "127.123456")
	@NotNull(message = "경도는 필수입니다.")
	Double longitude,
	@Schema(description = "가게 설명", example = "가게 설명")
	@NotBlank(message = "가게 설명은 필수입니다.")
	String description,
	@Schema(description = "가게 대표 이미지 URL", example = "https://example.com/image.jpg")
	@NotBlank(message = "가게 대표 이미지는 필수입니다.")
	String headImageUrl,
	@Schema(description = "가게 소속 대학교", example = "서울대학교")
	String university
) {
	public StoreInfo toStoreInfo() {
		return StoreInfo.of(
			storeName,
			new Point2D.Double(latitude, longitude),
			description,
			headImageUrl,
			university
		);
	}
}
