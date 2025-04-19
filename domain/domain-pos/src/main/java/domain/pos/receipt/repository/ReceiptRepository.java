package domain.pos.receipt.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;

public interface ReceiptRepository {
	Receipt createReceipt(UserPassport userPassport, Table table, Sale sale);

	Optional<ReceiptInfo> getReceiptInfo(Long receiptId);

	Optional<Receipt> getReceiptWithStore(Long receiptId);

	Optional<Receipt> getNonAdjustReceiptWithStore(Long receiptId);

	Page<Receipt> getReceiptPageBySale(Pageable pageable, Long saleId);

	boolean existsReceipt(Long receiptId);

	void adjustReceipt(Long receiptId);

	void deleteReceipt(Long receiptId);
}
