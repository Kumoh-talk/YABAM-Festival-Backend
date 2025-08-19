package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.port.required.SaleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleWriter {
	private final SaleRepository saleRepository;

	public Sale createSale(Store previousStore) {
		return saleRepository.createSale(previousStore);
	}

	public Sale closeSale(Sale savedSale, Store closeStore) {
		return saleRepository.closeSale(savedSale, closeStore);
	}
}
