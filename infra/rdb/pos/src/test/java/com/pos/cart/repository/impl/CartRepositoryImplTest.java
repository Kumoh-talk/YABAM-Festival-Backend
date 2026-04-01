package com.pos.cart.repository.impl;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static com.pos.receipt.ReceiptEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.cart.entity.CartEntity;
import com.pos.cart.entity.CartMenuEntity;
import com.pos.cart.repository.CartMenuJpaRepository;
import com.pos.global.config.RepositoryTest;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.repository.CartRepository;

class CartRepositoryImplTest extends RepositoryTest {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartMenuJpaRepository cartMenuJpaRepository;

	private ReceiptEntity savedReceiptEntity;
	private CartEntity savedCartEntity;

	@BeforeEach
	void setUp() {
		StoreEntity storeEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_OPEN_STORE()));
		TableEntity tableEntity = testFixtureBuilder.buildTableEntityList(TABLEENTITY_LIST(1, storeEntity)).get(0);
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(storeEntity));
		savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
			GENERAL_ADJUSTMENT_RECEIPT(saleEntity, tableEntity));
		savedCartEntity = testFixtureBuilder.buildCartEntity(CartEntity.from(savedReceiptEntity));

		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Nested
	@DisplayName("세션 진입")
	class EnterOrderSessionTest {

		@Test
		void 세션_진입_성공_DB에_sessionToken과_pendingAt_저장() {
			Cart result = cartRepository.enterOrderSession(savedReceiptEntity.getId());

			testEntityManager.flush();
			testEntityManager.clear();

			CartEntity updated = testEntityManager.find(CartEntity.class, savedCartEntity.getId());
			assertSoftly(softly -> {
				softly.assertThat(result.getSessionToken()).isNotNull();
				softly.assertThat(result.getPendingAt()).isNotNull();
				softly.assertThat(updated.getSessionToken()).isNotNull();
				softly.assertThat(updated.getPendingAt()).isNotNull();
			});
		}

		@Test
		void 장바구니_없으면_CART_NOT_FOUND() {
			UUID unknownReceiptId = UUID.randomUUID();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartRepository.enterOrderSession(unknownReceiptId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
			});
		}

		@Test
		void 이미_활성_세션이_있으면_CART_ORDER_SESSION_ACTIVE() {
			// given - 세션을 먼저 시작
			testEntityManager.find(CartEntity.class, savedCartEntity.getId())
				.startSession(UUID.randomUUID());
			testEntityManager.flush();
			testEntityManager.clear();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> cartRepository.enterOrderSession(savedReceiptEntity.getId()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_ACTIVE);
			});
		}
	}

	@Nested
	@DisplayName("세션 취소")
	class CancelOrderSessionTest {

		private UUID activeToken;

		@BeforeEach
		void givenActiveSession() {
			activeToken = UUID.randomUUID();
			testEntityManager.find(CartEntity.class, savedCartEntity.getId())
				.startSession(activeToken);
			testEntityManager.flush();
			testEntityManager.clear();
		}

		@Test
		void 세션_취소_성공_DB에서_sessionToken과_pendingAt_null() {
			cartRepository.cancelOrderSession(savedReceiptEntity.getId(), activeToken);

			testEntityManager.flush();
			testEntityManager.clear();

			CartEntity updated = testEntityManager.find(CartEntity.class, savedCartEntity.getId());
			assertSoftly(softly -> {
				softly.assertThat(updated.getSessionToken()).isNull();
				softly.assertThat(updated.getPendingAt()).isNull();
			});
		}

		@Test
		void 잘못된_토큰으로_취소_CART_ORDER_SESSION_INVALID() {
			UUID wrongToken = UUID.randomUUID();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> cartRepository.cancelOrderSession(savedReceiptEntity.getId(), wrongToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
			});
		}
	}

	@Nested
	@DisplayName("세션 활성 여부 확인")
	class IsCartPendingTest {

		@Test
		void 활성_세션_있으면_true() {
			testEntityManager.find(CartEntity.class, savedCartEntity.getId())
				.startSession(UUID.randomUUID());
			testEntityManager.flush();
			testEntityManager.clear();

			boolean result = cartRepository.isCartPending(savedReceiptEntity.getId());

			assertSoftly(softly -> softly.assertThat(result).isTrue());
		}

		@Test
		void 세션_없으면_false() {
			boolean result = cartRepository.isCartPending(savedReceiptEntity.getId());

			assertSoftly(softly -> softly.assertThat(result).isFalse());
		}

		@Test
		void 세션_만료_1분_경과_후_false() {
			// given - 1분 1초 전에 세션 시작 (만료)
			CartEntity cartEntity = testEntityManager.find(CartEntity.class, savedCartEntity.getId());
			cartEntity.startSession(UUID.randomUUID());
			testEntityManager.flush();

			// pendingAt을 만료 시간으로 직접 수정
			testEntityManager.getEntityManager()
				.createQuery("UPDATE CartEntity c SET c.pendingAt = :expiredAt WHERE c.id = :id")
				.setParameter("expiredAt", LocalDateTime.now().minusSeconds(61))
				.setParameter("id", savedCartEntity.getId())
				.executeUpdate();
			testEntityManager.flush();
			testEntityManager.clear();

			boolean result = cartRepository.isCartPending(savedReceiptEntity.getId());

			assertSoftly(softly -> softly.assertThat(result).isFalse());
		}
	}

	@Nested
	@DisplayName("FOR UPDATE 장바구니 조회")
	class GetCartWithLockTest {

		@Test
		void 장바구니_없으면_빈_Optional() {
			UUID unknownReceiptId = UUID.randomUUID();

			Optional<Cart> result = cartRepository.getCartWithLock(unknownReceiptId);

			assertSoftly(softly -> softly.assertThat(result).isEmpty());
		}

		@Test
		void 장바구니_있으면_Cart_반환() {
			Optional<Cart> result = cartRepository.getCartWithLock(savedReceiptEntity.getId());

			assertSoftly(softly -> {
				softly.assertThat(result).isPresent();
				softly.assertThat(result.get().getReceiptId()).isEqualTo(savedReceiptEntity.getId());
			});
		}
	}
}
