package domain.pos.table.service;

import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TableService {
	private final StoreValidator storeValidator;
	private final TableHandler tableHandler;

	public Table createTable(final UserPassport queryUserPassport, final Long queryStoreId,
		final Integer queryTableNumber) {
		final Store store = storeValidator.validateStoreByUser(queryUserPassport, queryStoreId);

		tableHandler.exitsTable(store, queryTableNumber)
			.ifPresent(table -> {
				log.warn("존재하는 테이블 생성 에러 : storeId={}, tableNumber={}", queryStoreId, queryTableNumber);
				throw new ServiceException(ErrorCode.EXIST_TABLE);
			});
		final Table createdTable = tableHandler.createTable(store, queryTableNumber);
		log.info("테이블 생성 성공 : storeId={}, tableNumber={}", queryStoreId, queryTableNumber);
		return createdTable;
	}
}
