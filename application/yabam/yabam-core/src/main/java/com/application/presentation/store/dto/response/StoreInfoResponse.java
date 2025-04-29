package com.application.presentation.store.dto.response;

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
	@Schema(description = "점주 고유 ID", example = "1")
	Long userId,
	@Schema(description = "점주 닉네임", example = "강민기")
	String userNickname,
	@Schema(description = "가게 이름", example = "가게 이름")
	String storeName,
	@Schema(description = "가게 위도", example = "37.123456")
	Double latitude,
	@Schema(description = "가게 경도", example = "127.123456")
	Double longitude,
	@Schema(description = "가게 설명", example = "가게 설명")
	String description,
	@Schema(description = "가게 대표 이미지 URL", example = "https://example.com/image.jpg")
	String headImageUrl,
	@Schema(description = "가게 소속 대학교", example = "서울대학교")
	String university,
	@Schema(description = "가게 테이블 시간", example = "30")
	Integer tableTime,
	@Schema(description = "가게 테이블 비용", example = "10000")
	Integer tableCost
) {
	public static StoreInfoResponse of(Store store) {
		return StoreInfoResponse.builder()
			.storeId(store.getStoreId())
			.isOpen(store.getIsOpen())
			.userId(store.getOwnerPassport().getUserId())
			.userNickname(store.getOwnerPassport().getUserNickname())
			.storeName(store.getStoreInfo().getStoreName())
			.latitude(store.getStoreInfo().getLocation().getX())
			.longitude(store.getStoreInfo().getLocation().getY())
			.description(store.getStoreInfo().getDescription())
			.headImageUrl(store.getStoreInfo().getHeadImageUrl())
			.university(store.getStoreInfo().getUniversity())
			.tableTime(store.getStoreInfo().getTableTime())
			.tableCost(store.getStoreInfo().getTableCost())
			.build();
	}
}
