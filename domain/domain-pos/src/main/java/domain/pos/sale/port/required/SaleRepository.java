package domain.pos.sale.port.required;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;

@Repository
public interface SaleRepository {
	Sale createSale(Store previousStore);

	Optional<Sale> findSaleWithStoreBySaleId(Long saleId);

	Optional<Sale> getOpenSaleByStoreId(Long storeId);

	Sale closeSale(Sale savedSale, Store closeStore);

	Slice<Sale> getSaleSliceByStoreId(Long storeId, Long lastSaleId, int size);

	Sale save(Sale sale);

	Optional<Sale> findOpenSaleByStoreId(Long storeId);

	Sale updateSale(Sale sale);

	boolean isExistsNonAdjustReceiptBySaleId(Long saleId);
}
