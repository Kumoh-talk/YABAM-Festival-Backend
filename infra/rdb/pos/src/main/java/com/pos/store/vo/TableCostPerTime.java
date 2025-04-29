package com.pos.store.vo;

import com.exception.VoException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TableCostPerTime {
	@Column(name = "table_time", nullable = false)
	private Integer tableTime;

	@Column(name = "table_cost", nullable = false)
	private Integer tableCost;

	private TableCostPerTime(Integer tableTime, Integer tableCost) {
		validate(tableTime, tableCost);
		this.tableTime = tableTime;
		this.tableCost = tableCost;
	}

	public static TableCostPerTime of(Integer tableTime, Integer tableCost) {
		return new TableCostPerTime(tableTime, tableCost);
	}

	private void validate(Integer tableTime, Integer tableCost) {
		if (tableTime <= 0) {
			throw new VoException("테이블 시간은 1보다 커야 합니다.");
		}
		if (tableCost < 0) {
			throw new VoException("테이블 비용은 음수일 수 없습니다.");
		}
	}
}
