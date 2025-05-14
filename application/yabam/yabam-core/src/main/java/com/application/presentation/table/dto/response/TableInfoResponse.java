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
		Long tableId,
		Integer tableNumber,
		Boolean isActive
	) {
		public static TableInfoDTO from(Table table) {
			return TableInfoDTO.builder()
				.tableId(table.getTableId())
				.tableNumber(table.getTableNumber())
				.isActive(table.getIsActive())
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
