package fixtures.table;

import domain.pos.table.entity.TableInfoRequest;
import domain.pos.table.entity.TablePoint;

public class TableInfoReqFixture {
	public static TableInfoRequest create() {
		return new TableInfoRequest(1, TablePoint.of(10, 20), 4);
	}
}
