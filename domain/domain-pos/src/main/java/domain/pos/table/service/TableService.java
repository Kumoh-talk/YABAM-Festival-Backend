package domain.pos.table.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
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

	public List<Table> createTable(final UserPassport ownerPassport, final Long queryStoreId,
		final Integer queryTableNumber) {
		final Store store = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);

		if (store.getIsOpen()) {
			log.warn("가게가 운영중입니다. 테이블 생성 불가 : storeId={}", queryStoreId);
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
		}

		if (tableReader.isExistsTable(store)) {
			log.warn("존재하는 테이블 생성 에러 : storeId={}, tableNumber={}", queryStoreId, queryTableNumber);
			throw new ServiceException(ErrorCode.EXIST_TABLE);
		}
		final List<Table> createdTable = tableWriter.createTables(store, queryTableNumber);
		log.info("테이블 생성 성공 : storeId={}, tableNumber={}", queryStoreId, queryTableNumber);
		return createdTable;
	}

	public void updateTableNum(UserPassport ownerPassport, Long queryStoreId,
		Integer queryUpdateTableNumber) {
		final Store store = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);

		if (store.getIsOpen()) {
			log.warn("가게가 운영중입니다. 테이블 수정 불가 : storeId={}", queryStoreId);
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_TABLE_WRITE);
		}

		tableWriter.modifyTableNum(store, queryUpdateTableNumber);
	}

	public List<Table> findTables(Long queryStoreId) {
		return tableReader.findTables(queryStoreId);
	}

}
