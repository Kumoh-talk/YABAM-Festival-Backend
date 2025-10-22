package domain.pos.receipt.port.provided;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vo.UserPassport;

import domain.pos.receipt.entity.v2.domain.Receipt;

public interface ReceiptRead {

	List<Receipt> readAllTableNonAdjusts(UserPassport userPassport, Long saleId);

	Page<Receipt> readAdjustsPageBySale(UserPassport userPassport, Pageable pageable, Long saleId);

	UUID readNonAdjustReceiptId(UUID tableId);

}
