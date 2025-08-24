package domain.pos.sale.entity;

import static com.exception.ErrorCode.*;
import static com.exception.State.*;

import java.time.LocalDateTime;
import java.util.Optional;

import domain.pos.store.entity.Store;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Sale {
	private Long id;
	private Long storeId;
	private LocalDateTime openDateTime;
	private Optional<LocalDateTime> closeDateTime;
	private Store store;

	private Sale(Long id, LocalDateTime openDateTime, LocalDateTime closeDateTime, Store store) {
		this.id = id;
		this.openDateTime = openDateTime;
		this.closeDateTime = Optional.ofNullable(closeDateTime);
		this.store = store;
	}

	public static Sale of(Long saleId,
		LocalDateTime openDateTime,
		LocalDateTime closeDateTime,
		Store store) {
		return new Sale(saleId,
			openDateTime,
			closeDateTime,
			store);
	}

	public static Sale createOpenSale(Long storeId) {
		var sale = new Sale();
		sale.openDateTime = LocalDateTime.now();
		sale.closeDateTime = Optional.empty();
		sale.storeId = storeId;

		return sale;
	}

	public void close(boolean isNotExistsNonAdjustReceipt) {
		state(this.closeDateTime.isEmpty(), CONFLICT_CLOSE_STORE);

		state(isNotExistsNonAdjustReceipt, NON_ADJUST_RECEIPT_DONT_CLOSE);

		this.closeDateTime = Optional.of(LocalDateTime.now());
	}
}
