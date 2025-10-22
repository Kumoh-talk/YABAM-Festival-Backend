package domain.pos.receipt.entity.v2.domain;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.exception.ServiceException;

import fixtures.receipt.v2.ReceiptFixture;

class ReceiptTest {
	@Nested
	@DisplayName("create 테스트")
	class createTest {
		@Test
		@DisplayName("영수증 생성 성공")
		void create_success() {
			// given
			Long saleId = 1L;
			UUID tableId = UUID.randomUUID();

			// when
			Receipt receipt = Receipt.create(saleId, tableId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(receipt.getId()).isNull();
				softly.assertThat(receipt.getUsageTime().getStart()).isNotNull();
				softly.assertThat(receipt.getUsageTime().getStop()).isNull();
				softly.assertThat(receipt.isAdjustment()).isFalse();
				softly.assertThat(receipt.getSaleId()).isEqualTo(saleId);
				softly.assertThat(receipt.getTableId()).isEqualTo(tableId);
			});
		}

		@Test
		@DisplayName("saleId가 null이면 NullPointerException")
		void saleId_null() {
			// given
			UUID tableId = UUID.randomUUID();

			// when -> then
			assertThatThrownBy(() -> Receipt.create(null, tableId))
				.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("tableId가 null이면 NullPointerException")
		void tableId_null() {
			// given
			Long saleId = 1L;

			// when -> then
			assertThatThrownBy(() -> Receipt.create(saleId, null))
				.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	@DisplayName("stopUsage 테스트")
	class stopUsageTest {
		@Test
		@DisplayName("영수증 사용 종료 성공")
		void stopUsage_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STARTED_RECEIPT();

			// when
			receipt.stopUsage();

			// then
			assertThat(receipt.getUsageTime().getStop()).isNotNull();
		}

		@Test
		@DisplayName("이미 사용 종료된 영수증은 사용 종료 불가")
		void already_stopped_receipt() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.stopUsage())
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.ALREADY_STOPPED_RECEIPT);
		}
	}

	@Nested
	@DisplayName("restartUsage 테스트")
	class restartUsageTest {
		@Test
		@DisplayName("영수증 사용 재개 성공")
		void restartUsage_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();

			// when
			receipt.restartUsage();

			// then
			assertThat(receipt.getUsageTime().getStop()).isNull();
		}

		@Test
		@DisplayName("이미 정산된 영수증은 사용 재개 불가")
		void already_adjusted_receipt() {
			// given
			Receipt receipt = ReceiptFixture.VALID_ADJUSTED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.restartUsage())
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
	}

	@Nested
	@DisplayName("adjust 테스트")
	class adjustTest {
		@Test
		@DisplayName("영수증 정산 성공")
		void adjust_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();

			// when
			receipt.adjust();

			// then
			assertThat(receipt.isAdjustment()).isTrue();
		}

		@Test
		@DisplayName("점유 시간이 종료되지 않으면 영수증 정산 불가")
		void usageTime_not_stopped() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STARTED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.adjust())
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.NOT_STOPPED_RECEIPT);
		}

		@Test
		@DisplayName("이미 정산된 영수증은 영수증 정산 불가")
		void already_adjusted_receipt() {
			// given
			Receipt receipt = ReceiptFixture.VALID_ADJUSTED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.adjust())
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}
	}

	@Nested
	@DisplayName("moveTable 테스트")
	class moveTableTest {
		@Test
		@DisplayName("테이블 이동 성공")
		void moveTable_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();
			UUID newTableId = UUID.randomUUID();

			// when
			receipt.moveTable(newTableId);

			// then
			assertThat(receipt.getTableId()).isEqualTo(newTableId);
		}

		@Test
		@DisplayName("이미 정산된 영수증은 테이블 이동 불가")
		void already_adjusted_receipt() {
			// given
			Receipt receipt = ReceiptFixture.VALID_ADJUSTED_RECEIPT();
			UUID newTableId = UUID.randomUUID();

			// when -> then
			assertThatThrownBy(() -> receipt.moveTable(newTableId))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}

		@Test
		@DisplayName("이동할 테이블 ID가 null이면 NullPointerException")
		void moveTableId_null() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.moveTable(null))
				.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	@DisplayName("syncStartUsageTime 테스트")
	class syncStartUsageTimeTest {
		@Test
		@DisplayName("시작 시간 동기화 성공")
		void syncStartUsageTime_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STARTED_RECEIPT();
			var newStartTime = receipt.getUsageTime().getStart().minusHours(1);

			// when
			receipt.syncStartUsageTime(newStartTime);

			// then
			assertThat(receipt.getUsageTime().getStart()).isEqualTo(newStartTime);
		}

		@Test
		@DisplayName("이미 정산된 영수증은 시작 시간 동기화 불가")
		void already_adjusted_receipt() {
			// given
			Receipt receipt = ReceiptFixture.VALID_ADJUSTED_RECEIPT();
			var newStartTime = receipt.getUsageTime().getStart().minusHours(1);

			// when -> then
			assertThatThrownBy(() -> receipt.syncStartUsageTime(newStartTime))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
				.isEqualTo(com.exception.ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}

		@Test
		@DisplayName("동기화할 시작 시간이 null이면 NullPointerException")
		void newStartTime_null() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STARTED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.syncStartUsageTime(null))
				.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	@DisplayName("calculateUnits 테스트")
	class calculateUnitsTest {
		@Test
		@DisplayName("점유 요금 계산 성공")
		void calculateUnits_success() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STOPPED_RECEIPT();

			// when
			long units = receipt.calculateUnits();

			// then
			assertThat(units).isEqualTo(2L);
		}

		@Test
		@DisplayName("점유 시간이 종료되지 않으면 점유 요금 계산 불가")
		void usageTime_not_stopped() {
			// given
			Receipt receipt = ReceiptFixture.VALID_STARTED_RECEIPT();

			// when -> then
			assertThatThrownBy(() -> receipt.calculateUnits())
				.isInstanceOf(IllegalStateException.class);
		}
	}
}
