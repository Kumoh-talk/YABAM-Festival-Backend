package com.application.presentation.receipt.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import domain.pos.receipt.entity.ReceiptInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "영수증 세부정보 응답 DTO")
public record ReceiptInfoResponse(
	@Schema(description = "영수증 id", example = "1")
	Long receiptId,
	@Schema(description = "정산 여부", example = "false")
	boolean isAdjustment,
	@Schema(description = "주문 시작 시간", example = "2023-10-01T10:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime startUsageTime,
	@Schema(description = "사용 종료 시간", example = "2023-10-01T12:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime stopUsageTime,
	@Schema(description = "테이블 요금", example = "5000")
	Integer occupancyFee
) {
	public static ReceiptInfoResponse from(ReceiptInfo receiptInfo) {
		return ReceiptInfoResponse.builder()
			.receiptId(receiptInfo.getReceiptId())
			.isAdjustment(receiptInfo.isAdjustment())
			.startUsageTime(receiptInfo.getStartUsageTime())
			.stopUsageTime(receiptInfo.getStopUsageTime())
			.occupancyFee(receiptInfo.getOccupancyFee())
			.build();
	}
}
