package domain.pos.sale.adapter.service;

import static com.exception.ErrorCode.*;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.sale.entity.Sale;
import domain.pos.sale.port.provided.SaleCommand;
import domain.pos.sale.port.provided.SaleRead;
import domain.pos.sale.port.required.SaleRepository;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.port.required.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleHandler implements SaleCommand, SaleRead {
	private final SaleRepository saleRepository;
	private final StoreRepository storeRepository;
	private final StoreValidator storeValidator;

	@Transactional
	@Override
	public Sale openStore(final UserPassport ownerPassport, final Long storeId) {
		var store = storeValidator.validateStoreOwnerWithLock(ownerPassport, storeId);

		store.openStore();

		storeRepository.updateIsOpen(store);

		Sale sale = Sale.createOpenSale(store.getId());

		return saleRepository.save(sale);
	}

	@Transactional
	@Override
	public Sale closeStore(final UserPassport ownerPassport, final Long storeId) {
		var store = storeValidator.validateStoreOwnerWithLock(ownerPassport, storeId);

		var sale = saleRepository.findOpenSaleByStoreId(storeId)
			.orElseThrow(() -> new ServiceException(NOT_FOUND_SALE));

		store.closeStore();

		storeRepository.updateIsOpen(store);

		sale.close(isNotExistsNonAdjustReceipt(sale));

		return saleRepository.updateSale(sale);
	}

	@Override
	public Slice<Sale> getSingleSalesByStore(final Long storeId, final Long lastSaleId, final int size) {
		return saleRepository.getSaleSliceByStoreId(storeId, lastSaleId, size);
	}

	private boolean isNotExistsNonAdjustReceipt(Sale sale) {
		return !saleRepository.isExistsNonAdjustReceiptBySaleId(sale.getId());
	}
}
