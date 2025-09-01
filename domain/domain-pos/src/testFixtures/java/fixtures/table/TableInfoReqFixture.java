package fixtures.table;

import domain.pos.table.entity.TableInfoRequest;
import domain.pos.table.entity.TablePoint;

public class TableInfoReqFixture {
	public static TableInfoRequest INFO_REQ_INIT() {
		return new TableInfoRequest(1, TablePoint.of(10, 20), 4);
	}

	public static TableInfoRequest INFO_REQ_DIFF() {
		return new TableInfoRequest(2, TablePoint.of(30, 40), 6);
	}
}
