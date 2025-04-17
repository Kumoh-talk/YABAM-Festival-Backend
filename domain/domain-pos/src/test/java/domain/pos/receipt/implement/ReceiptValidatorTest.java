package domain.pos.receipt.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import fixtures.member.UserFixture;
import fixtures.receipt.ReceiptFixture;

public class ReceiptValidatorTest {
	private final ReceiptValidator receiptValidator = new ReceiptValidator();

	@Nested
	@DisplayName("영수증 접근 권한 검증")
	class validateAccessToReceipt {
		private Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();

		@Test
		void 고객_영수증_접근_성공() {
			// given
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when -> then
			assertDoesNotThrow(() -> receiptValidator.validateAccessToReceipt(receipt, userPassport));
		}

		@Test
		void 점주_영수증_접근_성공() {
			// given
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when -> then
			assertDoesNotThrow(() -> receiptValidator.validateAccessToReceipt(receipt, userPassport));
		}

		@Test
		void 비회원_영수증_접근_성공() {
			// given
			receipt = ReceiptFixture.GENERAL_NON_MEMBER_RECEIPT();
			UserPassport userPassport = UserFixture.ANONYMOUS_USER_PASSPORT();

			// when -> then
			assertDoesNotThrow(() -> receiptValidator.validateAccessToReceipt(receipt, userPassport));
		}

		@Test
		void 영수증_접근_실패() {
			// given
			UserPassport userPassport = UserFixture.DIFF_OWNER_PASSPORT();

			// when
			ServiceException exception = assertThrows(ServiceException.class, () -> {
				receiptValidator.validateAccessToReceipt(receipt, userPassport);
			});

			// then
			assertEquals(ErrorCode.RECEIPT_ACCESS_DENIED, exception.getErrorCode());
		}
	}
}
