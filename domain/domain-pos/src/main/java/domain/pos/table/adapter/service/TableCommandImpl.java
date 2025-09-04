package domain.pos.table.adapter.service;

import static com.exception.ErrorCode.*;
import static com.exception.State.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TableInfoRequest;
import domain.pos.table.port.provided.TableCommand;
import domain.pos.table.port.provided.TableRead;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class TableCommandImpl implements TableCommand, TableRead {
	private final TableRepository tableRepository;
	private final StoreValidator storeValidator;

	@Transactional
	@Override
	public Table createTable(UserPassport passport, final Long storeId, TableInfoRequest request) {
		Store store = storeValidator.validateStoreOwner(passport, storeId);

		ifState(store.getIsOpen(), STORE_IS_OPEN_TABLE_WRITE);

		ifState(isExistsTableNum(request, store), EXIST_TABLE);

		var table = Table.create(storeId, request);

		return tableRepository.save(table);
	}

	private boolean isExistsTableNum(TableInfoRequest request, Store store) {
		return tableRepository.existsTableByStoreAndTableNumWithLock(store, request.tableNumber());
	}

	@Transactional
	@Override
	public Table updateTable(UserPassport passport, final UUID tableId, TableInfoRequest request) {
		Table table = validateTable(tableId);

		var store = storeValidator.validateStoreOwner(passport, table.getStoreId());

		ifState(store.getIsOpen(), STORE_IS_OPEN_TABLE_WRITE);

		if (isDiffTableNumAndReqNum(request.tableNumber(), table)) {
			ifState(isExistsTableNum(request, store), EXIST_TABLE);
		}

		table.modify(request);

		return tableRepository.save(table);
	}

	private Table validateTable(UUID tableId) {
		return tableRepository.findById(tableId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_TABLE));
	}

	private static boolean isDiffTableNumAndReqNum(Integer updateTableNumber, Table table) {
		return !table.getTableNumber().equals(updateTableNumber);
	}

	@Transactional
	@Override
	public void deleteTable(UserPassport passport, final UUID tableId) {
		Table table = validateTable(tableId);

		var store = storeValidator.validateStoreOwner(passport, table.getStoreId());

		ifState(store.getIsOpen(), STORE_IS_OPEN_TABLE_WRITE);

		tableRepository.deleteTable(table);
	}

	@Override
	public List<Table> findTables(Long storeId) {
		return tableRepository.findTablesByStoreId(storeId);
	}
}
