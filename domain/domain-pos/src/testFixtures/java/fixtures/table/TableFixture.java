package fixtures.table;

import java.util.UUID;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;

public class TableFixture {
	// 테이블 고유 ID
	private static final UUID GENERAL_TABLE_ID = UUID.randomUUID();

	// 테이블 번호
	private static final Integer GENERAL_TABLE_NUMBER = 1;

	// 테이블 활성화 여부
	private static final Boolean IS_ACTIVE = true;
	private static final Boolean IS_INACTIVE = false;

	// 테이블 좌표
	private static final TablePoint GENERAL_TABLE_POINT = TablePoint.of(0, 0);

	public static Table GENERAL_IN_ACTIVE_TABLE(Store store) {
		return Table.of(
			GENERAL_TABLE_ID,
			GENERAL_TABLE_NUMBER,
			IS_INACTIVE,
			GENERAL_TABLE_POINT,
			store
		);
	}

	public static Table GENERAL_ACTIVE_TABLE(Store store) {
		return Table.of(
			GENERAL_TABLE_ID,
			GENERAL_TABLE_NUMBER,
			IS_ACTIVE,
			GENERAL_TABLE_POINT,
			store
		);
	}

}
