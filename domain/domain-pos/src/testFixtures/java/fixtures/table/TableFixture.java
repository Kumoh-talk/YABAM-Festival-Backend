package fixtures.table;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

public class TableFixture {
	// 테이블 고유 ID
	private static final Long GENERAL_TABLE_ID = 1L;

	// 테이블 번호
	private static final Integer GENERAL_TABLE_NUMBER = 1;

	// 테이블 활성화 여부
	private static final Boolean IS_ACTIVE = true;
	private static final Boolean IS_INACTIVE = false;

	public static Table GENERAL_IN_ACTIVE_TABLE(Store store) {
		return Table.of(
			GENERAL_TABLE_ID,
			GENERAL_TABLE_NUMBER,
			IS_INACTIVE,
			store
		);
	}

	public static Table GENERAL_ACTIVE_TABLE(Store store) {
		return Table.of(
			GENERAL_TABLE_ID,
			GENERAL_TABLE_NUMBER,
			IS_ACTIVE,
			store
		);
	}

}
