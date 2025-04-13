package domain.pos.store.repository;

import java.util.Optional;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;

public interface SaleRepository {
	Sale createSale(Store previousStore);

	Optional<Sale> findSaleBySaleId(Long saleId);

	Optional<Sale> findSaleWithOwnerBySaleId(Long saleId);

	Sale closeSale(Sale savedSale, Store closeStore);
}
