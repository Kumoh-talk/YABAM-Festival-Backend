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
	Boolean hasNext,
	@Schema(description = "마지막 가게 ID", example = "1")
	Long lastStoreId,
	@Schema(description = "가게 데이터 리스트")
	List<StoreInfoDto> stores
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
		String thumbnailUrl,
		@Schema(description = "가게 위치", example = "A구역 3번 부스")
		String location,
		@Schema(description = "가게 소속 대학교", example = "서울대학교")
		String universityName,
		@Schema(description = "가게 설명", example = "가게 설명")
		String description,
		@Schema(description = "가게 상세 이미지 URL 리스트")
		List<String> detailImageUrls
	) {
		public static StoreInfoDto from(StoreHeadDto storeHeadDto) {
			return StoreInfoDto.builder()
				.storeId(storeHeadDto.getStoreId())
				.storeName(storeHeadDto.getStoreName())
				.isOpened(storeHeadDto.getIsOpened())
				.thumbnailUrl(storeHeadDto.getThumbnailUrl())
				.location(storeHeadDto.getLocation())
				.universityName(storeHeadDto.getUniversityName())
				.description(storeHeadDto.getDescription())
				.detailImageUrls(storeHeadDto.getDetailImageUrls())
				.build();
		}
	}

	public static StoreCursorResponse from(Slice<StoreHeadDto> storeHeadDtos) {
		int size = storeHeadDtos.getContent().size();
		return StoreCursorResponse.builder()
			.totalCount(size)
			.hasNext(storeHeadDtos.hasNext())
			.lastStoreId(storeHeadDtos.getContent().isEmpty() ? null :
				storeHeadDtos.getContent().get(size - 1).getStoreId())
			.stores(storeHeadDtos.getContent().stream()
				.map(StoreInfoDto::from)
				.toList())
			.build();
	}
}
