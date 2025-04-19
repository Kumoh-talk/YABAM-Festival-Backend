package com.pos.table.vo;

import com.exception.VoException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TableNumber {
	@Column(name = "table_number", nullable = false)
	private Integer tableNumber;

	private TableNumber(Integer tableNumber) {
		if (tableNumber <= 0) {
			throw new VoException("테이블 번호는 양의 정수이어야 합니다.");
		}
		this.tableNumber = tableNumber;
	}

	public static TableNumber from(Integer tableNumber) {
		return new TableNumber(tableNumber);
	}
}
