package com.application.presentation.store.dto.request;

import java.awt.geom.Point2D;

import domain.pos.store.entity.StoreInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
	String university,
	@Schema(description = "가게 테이블 시간", example = "30")
	@NotNull(message = "가게 테이블 시간은 필수입니다.")
	@Min(value = 1, message = "가게 테이블 시간은 1분 이상이어야 합니다.")
	Integer tableTime,
	@Schema(description = "가게 테이블 비용", example = "10000")
	@NotNull(message = "가게 테이블 비용은 필수입니다.")
	@Min(value = 0, message = "가게 테이블 비용은 0원 이상이어야 합니다.")
	Integer tableCost
) {
	public StoreInfo toStoreInfo() {
		return StoreInfo.of(
			storeName,
			new Point2D.Double(latitude, longitude),
			description,
			headImageUrl,
			university,
			tableTime,
			tableCost
		);
	}
}
