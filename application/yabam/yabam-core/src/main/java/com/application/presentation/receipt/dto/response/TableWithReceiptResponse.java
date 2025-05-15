package com.application.presentation.receipt.dto.response;

import java.util.List;

import com.application.presentation.order.dto.response.OrderInfoResponse;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.TableWithNonAdjustReceipt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 전체 테이블 및 미정산 영수증 응답 DTO")
public record TableWithReceiptResponse(

	@Schema(description = "테이블 및 영수증 정보 리스트", example = "테이블 및 영수증 정보 리스트")
	List<TableWithReceipt> tableWithReceipts
) {
	public static TableWithReceiptResponse from(List<TableWithNonAdjustReceipt> tableWithNonAdjustReceipts) {
		return new TableWithReceiptResponse(
			tableWithNonAdjustReceipts.stream()
				.map(TableWithReceipt::from)
				.toList()
		);
	}

	public record TableWithReceipt(
		@Schema(description = "테이블 id", example = "1")
		Long tableId,

		@Schema(description = "테이블 번호", example = "1")
		Integer tableNumber,

		@Schema(description = "테이블 활성화 여부(false -> 미정산 영수증을 가지고 있는 상태)", example = "true")
		Boolean isActive,

		@Schema(description = "영수증 및 누적 주문 정보", example = "영수증 및 누적 주문 정보")
		ReceiptAndOrders receiptInfo
	) {

		public static TableWithReceipt from(TableWithNonAdjustReceipt tableWithNonAdjustReceipt) {
			return new TableWithReceipt(
				tableWithNonAdjustReceipt.getTableId(),
				tableWithNonAdjustReceipt.getTableNumber(),
				tableWithNonAdjustReceipt.getIsActive(),
				ReceiptAndOrders.from(tableWithNonAdjustReceipt.getNonAdjustReceipt())
			);
		}

	}

	public record ReceiptAndOrders(

		@Schema(description = "영수증 상세 정보", example = "영수증 상세 정보")
		ReceiptInfoResponse receiptInfo,

		@Schema(description = "영수증 누적 주문 정보", example = "영수증 누적 주문 정보")
		List<OrderInfoResponse> orderInfo
	) {

		public static ReceiptAndOrders from(Receipt nonAdjustReceipt) {
			if (nonAdjustReceipt == null) {
				return new ReceiptAndOrders(null, List.of());
			}
			return new ReceiptAndOrders(ReceiptInfoResponse.from(nonAdjustReceipt.getReceiptInfo()),
				nonAdjustReceipt.getOrders().stream().map(OrderInfoResponse::from).toList());
		}

	}
}
