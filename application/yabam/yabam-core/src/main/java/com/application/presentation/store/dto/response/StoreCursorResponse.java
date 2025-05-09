package com.application.presentation.store.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import domain.pos.store.entity.dto.StoreHeadDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "StoreCursorResponse", description = "가게 커서 응답")
@Builder
public record StoreCursorResponse(
	@Schema(description = "데이터 수", example = "10")
	Integer totalCount,
	@Schema(description = "다음 데이터 존재 여부", example = "true")
	Boolean hasNextPage,
	@Schema(description = "마지막 데이터의 review 수", example = "30")
	Long lastReviewCount,
	@Schema(description = "마지막 가게 ID", example = "1")
	Long lastStoreId,
	@Schema(description = "가게 데이터 리스트", example = "가게 데이터 리스트")
	List<StoreInfoDto> storeInfoDtos
) {
	@Schema(name = "StoreInfoDto", description = "가게 데이터")
	@Builder
	public record StoreInfoDto(
		@Schema(description = "가게 ID", example = "1")
		Long storeId,
		@Schema(description = "가게 이름", example = "가게 이름")
		String storeName,
		@Schema(description = "가게 오픈 여부", example = "true")
		Boolean isOpened,
		@Schema(description = "가게 대표 이미지 URL", example = "https://example.com/image.jpg")
		String headImageUrl,
		@Schema(description = "가게 설명", example = "가게 설명")
		String description,
		@Schema(description = "가게 평균 평점", example = "43")
		Integer ratingAverage,
		@Schema(description = "가게 리뷰 수", example = "10")
		Integer reviewCount,
		@Schema(description = "가게 상세 이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
		List<String> storeDetailImageUrls
	) {
		public static StoreInfoDto from(StoreHeadDto storeHeadDto) {
			return StoreInfoDto.builder()
				.storeId(storeHeadDto.getStoreId())
				.storeName(storeHeadDto.getStoreName())
				.isOpened(storeHeadDto.getIsOpened())
				.headImageUrl(storeHeadDto.getHeadImageUrl())
				.description(storeHeadDto.getDescription())
				.ratingAverage(storeHeadDto.getRatingAverage())
				.reviewCount(storeHeadDto.getReviewCount())
				.storeDetailImageUrls(storeHeadDto.getStoreDetailImageUrls())
				.build();
		}
	}

	public static StoreCursorResponse from(Slice<StoreHeadDto> storeHeadDtos) {
		int size = storeHeadDtos.getContent().size();
		return StoreCursorResponse.builder()
			.totalCount(size)
			.hasNextPage(storeHeadDtos.hasNext())
			.lastReviewCount(Long.valueOf(storeHeadDtos.getContent().isEmpty() ? null :
				storeHeadDtos.getContent().get(size - 1).getReviewCount()))
			.lastStoreId(storeHeadDtos.getContent().isEmpty() ? null :
				storeHeadDtos.getContent().get(size - 1).getStoreId())
			.storeInfoDtos(storeHeadDtos.getContent().stream()
				.map(StoreInfoDto::from)
				.toList())
			.build();
	}
}
