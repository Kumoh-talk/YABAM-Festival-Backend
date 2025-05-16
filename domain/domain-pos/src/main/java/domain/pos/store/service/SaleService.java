package domain.pos.store.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.SaleReader;
import domain.pos.store.implement.SaleWriter;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.implement.StoreWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SaleService {
	private final SaleWriter saleWriter;
	private final SaleReader saleReader;
	private final StoreWriter storeWriter;
	private final StoreValidator storeValidator;

	@Transactional
	public Sale openStore(final UserPassport ownerPassport, final Long storeId) {
		final Store previousStore = storeValidator.validateStoreOwner(ownerPassport, storeId);

		if (previousStore.getIsOpen()) {
			log.warn("가게 활성화 변경 실패: userId={}, storeId={}", ownerPassport.getUserId(), storeId);
			throw new ServiceException(ErrorCode.CONFLICT_OPEN_STORE);
		}

		final Store opendStore = storeWriter.modifyStoreOpenStatus(previousStore);
		final Sale createdSale = saleWriter.createSale(opendStore);

		log.info("가게 활성화 성공 : userId={}, storeId={}, saleId={}", ownerPassport.getUserId(), storeId,
			createdSale.getSaleId());
		return createdSale;
	}

	@Transactional
	public Sale closeStore(final UserPassport ownerPassport, final Long saleId) {
		final Sale savedSale = saleReader.readSingleSale(saleId)
			.orElseThrow(() -> {
				log.warn("판매 내역 조회 실패: saleId={}", saleId);
				throw new ServiceException(ErrorCode.NOT_FOUND_STORE);
			});

		validateOpendSaleOrStore(ownerPassport, saleId, savedSale);

		final Store closedStore = storeWriter.modifyStoreOpenStatus(savedSale.getStore());
		final Sale closedSale = saleWriter.closeSale(savedSale, closedStore);
		log.info("가게 종료 성공 : userId={}, storeId={}, saleId={}", ownerPassport.getUserId(),
			savedSale.getStore().getStoreId(), closedSale.getSaleId());
		return closedSale;
	}

	// 판매 종료 시점에 가게가 종료된 상태인지 확인
	private static void validateOpendSaleOrStore(UserPassport ownerPassport, Long saleId, Sale savedSale) {
		savedSale.getCloseDateTime()
			.ifPresent((dateTime) -> {
				log.warn("이미 종료된 Sale.: userId={}, saleId={}", ownerPassport.getUserId(), saleId);
				throw new ServiceException(ErrorCode.CONFLICT_CLOSE_STORE);
			});
		if (!savedSale.getStore().getIsOpen()) {
			log.warn("이미 종료된 가게 상태: userId={}, storeId={}", ownerPassport.getUserId(),
				savedSale.getStore().getStoreId());
			throw new ServiceException(ErrorCode.CONFLICT_CLOSE_STORE);
		}
	}

	public Slice<Sale> getSingleSalesByStore(final Long storeId, final Long lastSaleId, final int size) {
		return saleReader.getSingleSalesByStore(storeId, lastSaleId, size);
	}

}
