package domain.pos.table.port.provided;

import java.util.List;

import domain.pos.table.entity.Table;

public interface TableRead {
	List<Table> findTables(Long storeId);
}
