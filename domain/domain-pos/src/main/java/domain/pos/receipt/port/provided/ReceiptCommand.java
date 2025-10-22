package domain.pos.receipt.port.provided;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vo.UserPassport;

import domain.pos.receipt.entity.v2.domain.Receipt;

public interface ReceiptCommand {

	Receipt create(UserPassport userPassport, final Long storeId, final UUID tableId);

	List<Receipt> stopUsage(UserPassport userPassport, List<UUID> receiptIds);

	void restartUsage(UserPassport userPassport, List<UUID> receiptIds);

	void adjust(UserPassport userPassport, List<UUID> receiptIds);

	void delete(UserPassport userPassport, UUID receiptId);

	void moveTable(
		final UserPassport userPassport,
		final UUID receiptId,
		final UUID moveTableId
	);

	LocalDateTime syncStartUsageTime(UserPassport userPassport, UUID baseReceiptId, List<UUID> receiptIds);
}
