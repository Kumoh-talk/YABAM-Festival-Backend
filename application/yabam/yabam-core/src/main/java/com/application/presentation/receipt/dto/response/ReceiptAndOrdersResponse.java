package com.application.presentation.receipt.dto.response;

import java.util.List;

import com.application.presentation.order.dto.response.OrderAndMenusResponse;

import domain.pos.receipt.entity.Receipt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영수증 및 주문 리스트 응답 DTO")
public record ReceiptAndOrdersResponse(
	@Schema(description = "영수증 정보", example = "영수증 정보")
	ReceiptInfoResponse receiptInfo,
	@Schema(description = "주문 리스트", example = "주문 리스트")
	List<OrderAndMenusResponse> orderAndMenusResponses
) {
	public static ReceiptAndOrdersResponse from(Receipt receipt) {
		return new ReceiptAndOrdersResponse(
			ReceiptInfoResponse.from(receipt.getReceiptInfo()),
			receipt.getOrders().stream().map(OrderAndMenusResponse::from).toList());
	}
}
