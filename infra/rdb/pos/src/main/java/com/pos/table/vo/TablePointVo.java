package com.pos.table.vo;

import domain.pos.table.entity.TablePoint;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TablePointVo {
	@Column(name = "table_x", nullable = false)
	private Integer tableX;

	@Column(name = "table_y", nullable = false)
	private Integer tableY;

	private TablePointVo(Integer tableX, Integer tableY) {
		this.tableX = tableX;
		this.tableY = tableY;
	}

	public static TablePointVo of(Integer tableX, Integer tableY) {
		return new TablePointVo(tableX, tableY);
	}

	public TablePoint toDomain() {
		return TablePoint.of(tableX, tableY);
	}
}
