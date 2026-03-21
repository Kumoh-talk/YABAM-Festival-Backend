package com.pos.payment.repository;

import static com.pos.fixtures.payment.PaymentEntityFixture.*;
import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static com.pos.receipt.ReceiptEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.global.config.RepositoryTest;
import com.pos.payment.entity.PaymentEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.repository.PaymentRepository;

class PaymentRepositoryImplTest extends RepositoryTest {

	@Autowired
	private PaymentRepository paymentRepository;

	private StoreEntity savedStoreEntity;
	private TableEntity savedTableEntity;
	private SaleEntity savedSaleEntity;
	private ReceiptEntity savedReceiptEntity;
	private PaymentEntity savedPaymentEntity;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_OPEN_STORE()));
		savedTableEntity = testFixtureBuilder.buildTableEntityList(TABLEENTITY_LIST(1, savedStoreEntity)).get(0);
		savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
			GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));
		savedPaymentEntity = testFixtureBuilder.buildPaymentEntity(
			GENERAL_DONE_PAYMENT_ENTITY(savedReceiptEntity));

		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Nested
	@DisplayName("결제 저장")
	class Save {

		@Test
		void 성공() {
			// given
			ReceiptEntity anotherReceipt = testFixtureBuilder.buildReceiptEntity(
				GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));
			testEntityManager.flush();
			testEntityManager.clear();

			Payment payment = paymentRepository.findByReceiptId(anotherReceipt.getId())
				.orElse(null);

			// when
			Payment newPayment = Payment.builder()
				.receiptId(anotherReceipt.getId())
				.tossPaymentKey("another_key_xyz")
				.tossOrderId(anotherReceipt.getId().toString())
				.amount(5000)
				.status(PaymentStatus.DONE)
				.paymentMethod("카드")
				.approvedAt(LocalDateTime.of(2024, 6, 1, 12, 0, 0))
				.build();
			Payment saved = paymentRepository.save(newPayment);
			testEntityManager.flush();
			testEntityManager.clear();

			// then
			assertSoftly(softly -> {
				softly.assertThat(payment).isNull();
				softly.assertThat(saved.getPaymentId()).isNotNull();
				softly.assertThat(saved.getAmount()).isEqualTo(5000);
				softly.assertThat(saved.getStatus()).isEqualTo(PaymentStatus.DONE);
			});
		}
	}

	@Nested
	@DisplayName("receiptId로 결제 조회")
	class FindByReceiptId {

		@Test
		void 성공() {
			// given
			UUID receiptId = savedReceiptEntity.getId();

			// when
			Optional<Payment> result = paymentRepository.findByReceiptId(receiptId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isPresent();
				softly.assertThat(result.get().getReceiptId()).isEqualTo(receiptId);
				softly.assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.DONE);
			});
		}

		@Test
		void 없으면_빈값_반환() {
			// given
			UUID nonExistentReceiptId = UUID.randomUUID();

			// when
			Optional<Payment> result = paymentRepository.findByReceiptId(nonExistentReceiptId);

			// then
			assertSoftly(softly -> softly.assertThat(result).isEmpty());
		}
	}

	@Nested
	@DisplayName("tossPaymentKey로 결제 조회")
	class FindByTossPaymentKey {

		@Test
		void 성공() {
			// given
			String paymentKey = savedPaymentEntity.getTossPaymentKey();

			// when
			Optional<Payment> result = paymentRepository.findByTossPaymentKey(paymentKey);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isPresent();
				softly.assertThat(result.get().getTossPaymentKey()).isEqualTo(paymentKey);
			});
		}

		@Test
		void 없으면_빈값_반환() {
			// given
			String nonExistentKey = "non_existent_key_xyz";

			// when
			Optional<Payment> result = paymentRepository.findByTossPaymentKey(nonExistentKey);

			// then
			assertSoftly(softly -> softly.assertThat(result).isEmpty());
		}
	}

	@Nested
	@DisplayName("결제 상태 업데이트")
	class UpdateStatus {

		@Test
		void 성공_DONE에서_CANCELED로() {
			// given
			Long paymentId = savedPaymentEntity.getId();

			// when
			Payment updated = paymentRepository.updateStatus(paymentId, PaymentStatus.CANCELED);
			testEntityManager.flush();
			testEntityManager.clear();

			// then
			assertSoftly(softly -> {
				softly.assertThat(updated.getPaymentId()).isEqualTo(paymentId);
				softly.assertThat(updated.getStatus()).isEqualTo(PaymentStatus.CANCELED);

				PaymentEntity entity = testEntityManager.find(PaymentEntity.class, paymentId);
				softly.assertThat(entity.getStatus()).isEqualTo(PaymentStatus.CANCELED);
			});
		}

		@Test
		void 실패_존재하지_않는_결제() {
			// given
			Long invalidPaymentId = 999L;

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> paymentRepository.updateStatus(invalidPaymentId, PaymentStatus.CANCELED))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
			});
		}
	}
}
