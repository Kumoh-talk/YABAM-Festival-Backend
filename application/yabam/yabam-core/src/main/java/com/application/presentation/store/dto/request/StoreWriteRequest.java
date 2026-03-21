package com.application.presentation.store.dto.request;

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
	@Schema(description = "가게 위치", example = "A구역 3번 부스")
	@NotBlank(message = "가게 위치는 필수입니다.")
	String location,
	@Schema(description = "가게 설명", example = "가게 설명")
	String description,
	@Schema(description = "가게 대표 이미지 URL", example = "https://example.com/image.jpg")
	@NotBlank(message = "가게 대표 이미지는 필수입니다.")
	String thumbnailUrl,
	@Schema(description = "가게 소속 대학교", example = "서울대학교")
	@NotBlank(message = "가게 소속 대학교는 필수입니다.")
	String universityName,
	@Schema(description = "가게 테이블 시간", example = "1")
	@NotNull(message = "가게 테이블 시간은 필수입니다.")
	@Min(value = 1, message = "가게 테이블 시간은 1시간 이상이어야 합니다.")
	Integer tableTime,
	@Schema(description = "가게 테이블 비용", example = "10000")
	@NotNull(message = "가게 테이블 비용은 필수입니다.")
	@Min(value = 0, message = "가게 테이블 비용은 0원 이상이어야 합니다.")
	Integer tableCost
) {
	public StoreInfo toStoreInfo() {
		return StoreInfo.of(
			storeName,
			location,
			description,
			thumbnailUrl,
			universityName,
			tableTime,
			tableCost
		);
	}
}
