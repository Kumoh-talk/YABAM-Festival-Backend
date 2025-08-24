package domain.pos.store.entity;

import static com.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.ServiceException;

import domain.pos.sale.entity.Sale;

class SaleTest {
	@Test
	void createSaleTest() {
		Sale sale = Sale.createOpenSale(1L);

		assertThat(sale).isNotNull();
		assertThat(sale.getStoreId()).isEqualTo(1L);
		assertThat(sale.getOpenDateTime()).isNotNull();
		assertThat(sale.getCloseDateTime()).isEmpty();
	}

	@Test
	void closeTest() {
		Sale sale = Sale.createOpenSale(1L);
		boolean isNotExistsNonAdjustReceipt = true;

		assertThat(sale.getCloseDateTime()).isEmpty();

		sale.close(isNotExistsNonAdjustReceipt);

		assertThat(sale.getCloseDateTime()).isNotEmpty();
	}

	@Test
	void closeFailTest() {
		Sale sale = Sale.createOpenSale(1L);
		boolean isNotExistsNonAdjustReceipt = true;

		assertThat(sale.getCloseDateTime()).isEmpty();

		// 미정산 영수증이 있다면 종료 불가
		assertThatThrownBy(() ->
			sale.close(!isNotExistsNonAdjustReceipt)
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NON_ADJUST_RECEIPT_DONT_CLOSE);

		sale.close(isNotExistsNonAdjustReceipt);

		assertThat(sale.getCloseDateTime()).isNotEmpty();

		// 이미 종료된 판매내역은 종료 불가
		assertThatThrownBy(() ->
			sale.close(isNotExistsNonAdjustReceipt)
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", CONFLICT_CLOSE_STORE);
	}

}
