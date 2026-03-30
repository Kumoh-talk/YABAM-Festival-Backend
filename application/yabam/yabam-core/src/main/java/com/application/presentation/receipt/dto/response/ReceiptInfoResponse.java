package com.application.presentation.receipt.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import domain.pos.receipt.entity.Receipt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "영수증 세부정보 응답 DTO")
public record ReceiptInfoResponse(
	@Schema(description = "영수증 id", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID receiptId,
	@Schema(description = "영업 id", example = "1")
	Long saleId,
	@Schema(description = "테이블 id", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID tableId,
	@Schema(description = "테이블 번호", example = "1")
	Integer tableNumber,
	@Schema(description = "정산 여부", example = "false")
	boolean isAdjusted,
	@Schema(description = "시간 정지 여부", example = "false")
	boolean usageStopped,
	@Schema(description = "이용 시작 시간", example = "2023-10-01T10:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createdAt,
	@Schema(description = "정산(시간정지) 시간", example = "2023-10-01T12:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime adjustedAt,
	@Schema(description = "테이블 요금", example = "5000")
	Integer occupancyFee
) {
	public static ReceiptInfoResponse from(Receipt receipt) {
		var info = receipt.getReceiptInfo();
		return ReceiptInfoResponse.builder()
			.receiptId(info.getReceiptId())
			.saleId(receipt.getSale() != null ? receipt.getSale().getId() : null)
			.tableId(receipt.getTable() != null ? receipt.getTable().getId() : null)
			.tableNumber(receipt.getTable() != null ? receipt.getTable().getTableNumber().value() : null)
			.isAdjusted(info.isAdjustment())
			.usageStopped(info.getStopUsageTime() != null)
			.createdAt(info.getStartUsageTime())
			.adjustedAt(info.getStopUsageTime())
			.occupancyFee(info.getOccupancyFee())
			.build();
	}
}
