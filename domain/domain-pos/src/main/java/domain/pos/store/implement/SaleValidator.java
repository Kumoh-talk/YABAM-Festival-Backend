package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.store.entity.Sale;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SaleValidator {
	public void validateSaleOpen(Sale sale) {
		if (sale.getCloseDateTime().isPresent()) {
			log.warn("영업이 이미 종료되었습니다. saleId: {}", sale.getId());
			throw new ServiceException(ErrorCode.CLOSE_SALE);
		}
	}
}
