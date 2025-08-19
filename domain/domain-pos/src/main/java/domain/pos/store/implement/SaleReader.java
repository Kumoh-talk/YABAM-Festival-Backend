package domain.pos.store.implement;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.store.entity.Sale;
import domain.pos.store.port.required.SaleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleReader {
	private final SaleRepository saleRepository;

	public Optional<Sale> readSingleSale(Long saleId) {
		return saleRepository.findSaleWithStoreBySaleId(saleId);
	}

	public Optional<Sale> getOpenSaleByStoreId(Long storeId) {
		return saleRepository.getOpenSaleByStoreId(storeId);
	}

	public Slice<Sale> getSingleSalesByStore(Long storeId, Long lastSaleId, int size) {
		return saleRepository.getSaleSliceByStoreId(storeId, lastSaleId, size);
	}
}
