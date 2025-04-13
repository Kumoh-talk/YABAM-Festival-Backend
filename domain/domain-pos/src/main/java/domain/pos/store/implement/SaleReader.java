package domain.pos.store.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Sale;
import domain.pos.store.repository.SaleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleReader {
	private final SaleRepository saleRepository;

	public Optional<Sale> readSingleSale(Long saleId) {
		return saleRepository.findSaleBySaleId(saleId);
	}

	public Optional<Sale> readSaleWithOwner(Long saleId) {
		return saleRepository.findSaleWithOwnerBySaleId(saleId);
	}

}
