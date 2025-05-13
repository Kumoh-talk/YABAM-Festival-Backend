package com.application.presentation.call.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Slice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import domain.pos.call.entity.Call;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "CallCursorResponse", description = "직원 호출 커서 응답")
@Builder
public record CallCursorResponse(
	@Schema(description = "데이터 수", example = "10")
	Integer totalCount,
	@Schema(description = "다음 데이터 존재 여부", example = "true")
	Boolean hasNextPage,
	@Schema(description = "판매 단위 고유 ID", example = "1")
	Long saleId,
	@Schema(description = "직원 호출 데이터 정보")
	List<CallInfoDto> callInfoDtos
) {
	@Builder
	public record CallInfoDto(
		@Schema(description = "직원 호출 고유 ID", example = "1")
		Long callId,
		@Schema(description = "직원 호출 메시지", example = "물 주세요.")
		String callMessage,
		@Schema(description = "직원 호출 완료 여부", example = "false")
		Boolean isCompleted,
		@Schema(description = "테이블 고유 ID", example = "1")
		Long tableId,
		@Schema(description = "테이블 번호", example = "1")
		Integer tableNumber,
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		@Schema(description = "직원 호출 생성 시간", example = "2023-10-01T12:00:00")
		LocalDateTime createdAt
	) {
	}

	public static CallCursorResponse from(Slice<Call> calls) {
		List<CallInfoDto> callInfoDtos = calls.getContent().stream()
			.map(call -> {
				return CallInfoDto.builder()
					.callId(call.getCallId())
					.callMessage(call.getCallMessage().getMessage())
					.tableId(call.getTableCallInfo().getTableId())
					.tableNumber(call.getTableCallInfo().getTableNumber())
					.createdAt(call.getCreatedAt())
					.build();
			})
			.toList();
		return CallCursorResponse.builder()
			.totalCount(calls.getNumberOfElements())
			.hasNextPage(calls.hasNext())
			.saleId(calls.getContent().get(0).getSaleId())
			.callInfoDtos(callInfoDtos)
			.build();
	}
}
