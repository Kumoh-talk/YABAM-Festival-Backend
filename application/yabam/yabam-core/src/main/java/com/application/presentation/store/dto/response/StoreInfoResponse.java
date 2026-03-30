package com.application.presentation.store.dto.response;

import java.util.List;

import domain.pos.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "가게 정보 응답 DTO")
@Builder
public record StoreInfoResponse(
	@Schema(description = "가게 ID", example = "1")
	Long storeId,
	@Schema(description = "가게 오픈 여부", example = "true")
	boolean isOpen,
	@Schema(description = "가게 이름", example = "가게 이름")
	String storeName,
	@Schema(description = "가게 위치", example = "A구역 3번 부스")
	String location,
	@Schema(description = "가게 설명", example = "가게 설명")
	String description,
	@Schema(description = "가게 대표 이미지 URL", example = "https://example.com/image.jpg")
	String thumbnailUrl,
	@Schema(description = "가게 소속 대학교", example = "서울대학교")
	String universityName,
	@Schema(description = "가게 테이블 시간", example = "1")
	Integer tableTime,
	@Schema(description = "가게 테이블 비용", example = "10000")
	Integer tableCost,
	@Schema(description = "가게 상세 이미지 URL", example = "[\"https://example.com/image1.jpg\"]")
	List<String> detailImageUrls
) {
	public static StoreInfoResponse of(Store store) {
		return StoreInfoResponse.builder()
			.storeId(store.getId())
			.isOpen(store.getIsOpen())
			.storeName(store.getStoreInfo().getStoreName())
			.location(store.getStoreInfo().getLocation())
			.description(store.getStoreInfo().getDescription())
			.thumbnailUrl(store.getStoreInfo().getThumbnailUrl())
			.universityName(store.getStoreInfo().getUniversityName())
			.tableTime(store.getStoreInfo().getTableTime())
			.tableCost(store.getStoreInfo().getTableCost())
			.detailImageUrls(store.getDetailImageUrls())
			.build();
	}
}
