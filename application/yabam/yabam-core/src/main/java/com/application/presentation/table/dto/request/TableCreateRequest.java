package com.application.presentation.table.dto.request;

import domain.pos.table.entity.TablePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "테이블 생성 요청 DTO", name = "TableCreateRequest")
public record TableCreateRequest(
	@Schema(description = "가게 ID", example = "1")
	@NotNull
	Long storeId,
	@Schema(description = "테이블 X 좌표", example = "1")
	Integer tableX,
	@Schema(description = "테이블 Y 좌표", example = "1")
	Integer tableY,
	@Schema(description = "테이블 번호", example = "2")
	@Min(value = 1, message = "테이블 번호는 1 이상이어야 합니다.")
	Integer tableNumber,
	@Schema(description = "테이블 수용 인원", example = "4")
	@Min(value = 1, message = "최대 수용 인원은 1 이상이어야 합니다.")
	Integer tableCapacity
) {
	public TablePoint getTablePoint() {
		return TablePoint.of(this.tableX, this.tableY);
	}

	@AssertTrue(message = "테이블 수용 인원은 4 또는 6만 가능합니다.")
	public boolean isValidTableCapacity() {
		return tableCapacity == 4 || tableCapacity == 6;
	}
}
