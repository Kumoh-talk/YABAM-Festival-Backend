package com.application.presentation.sale.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Slice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import domain.pos.store.entity.Sale;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "SaleCursorResponse", description = "영업 커서 응답")
@Builder
public record SaleCursorResponse(
	@Schema(description = "데이터 수", example = "10")
	Integer totalCount,
	@Schema(description = "다음 데이터 존재 여부", example = "true")
	Boolean hasNextPage,
	@Schema(description = "마지막 영업 ID", example = "1")
	Long lastSaleId,
	@Schema(description = "영업 데이터 리스트", example = "영업 데이터 리스트")
	List<SaleInfoDto> saleInfoDtos
) {
	public static SaleCursorResponse from(Slice<Sale> saleSlice) {
		List<SaleInfoDto> saleInfoDtos = saleSlice.getContent().stream()
			.map(SaleInfoDto::from)
			.toList();
		return SaleCursorResponse.builder()
			.totalCount(saleSlice.getNumberOfElements())
			.hasNextPage(saleSlice.hasNext())
			.lastSaleId(saleSlice.getContent().isEmpty() ? null :
				saleSlice.getContent().get(saleSlice.getNumberOfElements() - 1).getSaleId())
			.saleInfoDtos(saleInfoDtos)
			.build();
	}

	@Schema(name = "SaleInfoDto", description = "영업 데이터")
	@Builder
	public record SaleInfoDto(
		@Schema(description = "영업 ID", example = "1")
		Long saleId,
		@Schema(description = "영업 시작 시간", example = "2023-10-01T10:00:00")
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		LocalDateTime openDateTime,
		@Schema(description = "영업 종료 시간", example = "2023-10-01T18:00:00")
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		LocalDateTime closeDateTime,
		@Schema(description = "가게 오픈 여부", example = "true")
		Boolean isOpen
	) {
		public static SaleInfoDto from(Sale sale) {
			return SaleInfoDto.builder()
				.saleId(sale.getSaleId())
				.openDateTime(sale.getOpenDateTime())
				.closeDateTime(sale.getCloseDateTime().orElse(null))
				.isOpen(sale.getCloseDateTime().get() == null ? true : false)
				.build();
		}
	}

}
