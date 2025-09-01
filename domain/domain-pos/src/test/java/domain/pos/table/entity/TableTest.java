package domain.pos.table.entity;

import static fixtures.table.TableInfoReqFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TableTest {
	@Test
	void createTableTest() {
		var storeId = 1L;
		TableInfoRequest request = INFO_REQ_INIT();

		Table table = Table.create(storeId, request);

		assertThat(table.getStoreId()).isEqualTo(storeId);
		assertThat(table.getTableNumber()).isEqualTo(request.tableNumber());
		assertThat(table.getTablePoint()).isEqualTo(request.tablePoint());
		assertThat(table.getTableCapacity()).isEqualTo(request.tableCapacity());
		assertThat(table.getIsActive()).isFalse();
	}

	@Test
	void modifyTableTest() {
		TableInfoRequest createRequest = INFO_REQ_INIT();
		TableInfoRequest updateRequest = INFO_REQ_DIFF();
		Table table = Table.create(1L, createRequest);

		table.modify(updateRequest);

		assertThat(table.getTableNumber()).isEqualTo(updateRequest.tableNumber());
		assertThat(table.getTablePoint()).isEqualTo(updateRequest.tablePoint());
		assertThat(table.getTableCapacity()).isEqualTo(updateRequest.tableCapacity());

	}

}
