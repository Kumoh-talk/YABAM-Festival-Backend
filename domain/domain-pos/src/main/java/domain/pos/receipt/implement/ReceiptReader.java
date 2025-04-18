package domain.pos.receipt.implement;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptReader {
	private final ReceiptRepository receiptRepository;

	public boolean existsReceipt(Long receiptId) {
		return receiptRepository.existsReceipt(receiptId);
	}

	public Optional<ReceiptInfo> getReceiptInfo(Long receiptId) {
		return receiptRepository.getReceiptInfo(receiptId);
	}

	public Optional<Receipt> getReceiptWithOwner(Long receiptId) {
		return receiptRepository.getReceiptWithOwner(receiptId);
	}

	public Optional<Receipt> getNonAdjustReceiptWithOwner(Long receiptId) {
		return receiptRepository.getNonAdjustReceiptWithOwner(receiptId);
	}

	public Page<Receipt> getReceiptPageBySale(Pageable pageable, Long saleId) {
		return receiptRepository.getReceiptPageBySale(pageable, saleId);
	}
}
