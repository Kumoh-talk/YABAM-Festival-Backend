package domain.pos.sale.port.provided;

import com.vo.UserPassport;

import domain.pos.sale.entity.Sale;

public interface SaleCommand {
	Sale openStore(UserPassport ownerPassport, Long storeId);

	Sale closeStore(UserPassport ownerPassport, Long storeId);
}
