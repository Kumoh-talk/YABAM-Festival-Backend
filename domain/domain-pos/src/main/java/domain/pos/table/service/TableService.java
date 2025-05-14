package domain.pos.table.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TableService {
	private final StoreValidator storeValidator;
	private final TableReader tableReader;
	private final TableWriter tableWriter;

	@Transactional
	public Long createTable(
		final UserPassport ownerPassport,
		final Long queryStoreId,
		final Integer tableNumber,
		final TablePoint tablePoint) {
		final Store store = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);

		if (store.getIsOpen()) {
			log.warn("가게가 운영중입니다. 테이블 생성 불가 : storeId={}", queryStoreId);
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
		}

		if (tableReader.isExistsTableByStoreAndTableNumWithLock(store, tableNumber)) {
			log.warn("존재하는 테이블 생성 에러 : storeId={}, tableNumber={}", queryStoreId, tableNumber);
			throw new ServiceException(ErrorCode.EXIST_TABLE);
		}
		return tableWriter.createTable(store, tableNumber, tablePoint);
	}

	@Transactional
	public void updateTable(
		final UserPassport ownerPassport,
		final Long qureyTableId,
		final Integer updateTableNumber,
		final TablePoint updateTablePoint) {
		final Table table = tableReader.findTableWithStoreByTableId(qureyTableId)
			.orElseThrow(() -> {
				log.warn("해당 테이블 존재하지 않음 : tableId={}", qureyTableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});
		if (isStoreOwnerOfTable(ownerPassport, table)) {
			log.warn("요청 유저는 테이블 소유자와 다름 : userId={}, tableId={}", ownerPassport.getUserId(), qureyTableId);
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
		if (table.getStore().getIsOpen()) {
			log.warn("가게가 운영중입니다. 테이블 생성 불가 : storeId={}", table.getStore().getStoreId());
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
		}
		if (isDiffTableNumAndQueryNum(updateTableNumber, table)) {
			if (tableReader.isExistsTableByStoreAndTableNumWithLock(table.getStore(), updateTableNumber)) {
				log.warn("존재하는 테이블 수정 에러 : storeId={}, tableNumber={}",
					table.getStore().getStoreId(),
					updateTableNumber);
				throw new ServiceException(ErrorCode.EXIST_TABLE);
			}
		}
		tableWriter.updateTable(table, updateTableNumber, updateTablePoint);
	}

	private static boolean isDiffTableNumAndQueryNum(Integer updateTableNumber, Table table) {
		return !table.getTableNumber().equals(updateTableNumber);
	}

	private static boolean isStoreOwnerOfTable(UserPassport ownerPassport, Table table) {
		return !table.getStore().getOwnerPassport().getUserId().equals(ownerPassport.getUserId());
	}

	@Transactional
	public void deleteTable(
		final UserPassport ownerPassport,
		final Long queryTableId) {
		final Table table = tableReader.findTableWithStoreByTableId(queryTableId)
			.orElseThrow(() -> {
				log.warn("해당 테이블 존재하지 않음 : tableId={}", queryTableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});
		if (isStoreOwnerOfTable(ownerPassport, table)) {
			log.warn("요청 유저는 테이블 소유자와 다름 : userId={}, tableId={}", ownerPassport.getUserId(), queryTableId);
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
		if (table.getStore().getIsOpen()) {
			log.warn("가게가 운영중입니다. 테이블 삭제 불가 : storeId={}", table.getStore().getStoreId());
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
		}
		tableWriter.deleteTable(table);
	}

	public List<Table> findTables(Long queryStoreId) {
		return tableReader.findTables(queryStoreId);
	}

}
