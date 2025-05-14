package domain.pos.table.entity;

import lombok.Getter;

@Getter
public class TablePoint {
	private final int tableX;
	private final int tableY;

	private TablePoint(int tableX, int tableY) {
		this.tableX = tableX;
		this.tableY = tableY;
	}

	public static TablePoint of(int tableX, int tableY) {
		return new TablePoint(tableX, tableY);
	}
}
