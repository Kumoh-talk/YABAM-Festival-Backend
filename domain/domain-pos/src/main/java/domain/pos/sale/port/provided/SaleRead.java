package domain.pos.sale.port.provided;

import org.springframework.data.domain.Slice;

import domain.pos.sale.entity.Sale;

public interface SaleRead {
	Slice<Sale> getSingleSalesByStore(Long storeId, Long lastSaleId, int size);
}
