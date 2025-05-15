package com.application.presentation.table.dto.response;

import java.util.List;

import domain.pos.table.entity.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema
@Builder
public record TableInfoResponse(
	@Schema(description = "테이블 정보 리스트")
	List<TableInfoDTO> tableInfoList
) {
	@Schema(description = "테이블 정보 DTO")
	@Builder
	public record TableInfoDTO(
		@Schema(description = "테이블 ID", example = "1")
		Long tableId,
		@Schema(description = "테이블 번호", example = "1")
		Integer tableNumber,
		@Schema(description = "테이블 활성화 여부", example = "true")
		Boolean isActive,
		@Schema(description = "테이블 X 좌표", example = "100")
		Integer tableX,
		@Schema(description = "테이블 Y 좌표", example = "200")
		Integer tableY
	) {
		public static TableInfoDTO from(Table table) {
			return TableInfoDTO.builder()
				.tableId(table.getTableId())
				.tableNumber(table.getTableNumber())
				.isActive(table.getIsActive())
				.tableX(table.getTablePoint().getTableX())
				.tableY(table.getTablePoint().getTableY())
				.build();
		}
	}

	public static TableInfoResponse from(List<Table> tableList) {
		return TableInfoResponse.builder()
			.tableInfoList(tableList.stream()
				.map(TableInfoDTO::from)
				.toList())
			.build();
	}
}
