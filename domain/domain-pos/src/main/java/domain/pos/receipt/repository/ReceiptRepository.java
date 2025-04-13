package domain.pos.receipt.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;

public interface ReceiptRepository {
	Receipt createReceipt(UserPassport userPassport, Table table, Sale sale);

	Optional<Receipt> getReceiptWithOwner(Long receiptId);

	Optional<Receipt> getReceiptWithCustomerAndOwner(Long receiptId);

	Page<Receipt> getReceiptPageBySale(Pageable pageable, Long saleId);

	void adjustReceipt(Long receiptId);

	void deleteReceipt(Long receiptId);
}
