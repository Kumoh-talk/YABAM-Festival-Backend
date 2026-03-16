package com.application.presentation.receipt.dto.response;

import com.application.presentation.table.dto.response.TableInfoResponse;

import domain.pos.receipt.entity.Receipt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영수증 응답 DTO")
public record ReceiptResponse(
	@Schema(description = "영수증 정보", example = "영수증 정보")
	ReceiptInfoResponse receiptInfo,
	@Schema(description = "테이블 정보", example = "테이블 정보")
	TableInfoResponse.TableInfoDTO tableInfo
) {
	public static ReceiptResponse from(Receipt receipt) {
		return new ReceiptResponse(
			ReceiptInfoResponse.from(receipt.getReceiptInfo()),
			TableInfoResponse.TableInfoDTO.from(receipt.getTable())
		);
	}
}
