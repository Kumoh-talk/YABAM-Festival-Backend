package com.application.presentation.sale.dto.response;

import domain.pos.store.entity.Sale;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "가게 오픈 정보 응답")
@Builder
public record SaleIdResponse(
	Long saleId
) {
	public static SaleIdResponse from(final Sale sale) {
		return SaleIdResponse.builder()
			.saleId(sale.getId())
			.build();
	}
}
