package domain.pos.order.service;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import base.ServiceTest;
import domain.pos.cart.entity.Cart;
import domain.pos.cart.implement.CartWriter;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptCustomerWriter;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.store.implement.SaleReader;
import domain.pos.store.implement.SaleValidator;
import fixtures.cart.CartFixture;
import fixtures.member.UserFixture;
import fixtures.order.OrderFixture;
import fixtures.receipt.ReceiptFixture;

public class OrderServiceTest extends ServiceTest {
	@Mock
	private ReceiptValidator receiptValidator;
	@Mock
	private SaleValidator saleValidator;
	@Mock
	private ReceiptCustomerWriter receiptCustomerWriter;
	@Mock
	private ReceiptReader receiptReader;
	@Mock
	private MenuReader menuReader;
	@Mock
	private OrderWriter orderWriter;
	@Mock
	private OrderReader orderReader;
	@Mock
	private CartWriter cartWriter;
	@Mock
	private SaleReader saleReader;

	@InjectMocks
	private OrderService orderService;

	@Nested
	@DisplayName("장바구니 기반 주문 생성 (세션 토큰 검증)")
	class PostOrderWithCart {
		private final UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		private final UUID sessionToken = CartFixture.SESSION_TOKEN;
		private final UserPassport userPassport = UserFixture.ANONYMOUS_USER_PASSPORT();
		private final Receipt receipt = ReceiptFixture.GENERAL_NON_ADJUSTMENT_RECEIPT();
		private final Order expectedOrder = OrderFixture.GENERAL_ORDER();

		@Test
		void 유효한_세션_토큰으로_주문_생성_성공() {
			Cart cart = CartFixture.CART_WITH_SESSION();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(cart));
			given(menuReader.countByIdIn(anyLong(), any())).willReturn(1L);
			given(orderWriter.postOrderWithCart(any(), any())).willReturn(expectedOrder);

			Order result = orderService.postOrderWithCart(receiptId, userPassport, sessionToken);

			assertSoftly(softly -> {
				softly.assertThat(result).isEqualTo(expectedOrder);
				verify(orderWriter).postOrderWithCart(any(), any());
				verify(cartWriter).deleteCartAndCartMenuByReceiptId(receiptId);
			});
		}

		@Test
		void 잘못된_세션_토큰으로_주문_CART_ORDER_SESSION_INVALID() {
			Cart cart = CartFixture.CART_WITH_SESSION();
			UUID wrongToken = UUID.randomUUID();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(cart));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderService.postOrderWithCart(receiptId, userPassport, wrongToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
				verify(orderWriter, never()).postOrderWithCart(any(), any());
			});
		}

		@Test
		void 세션_토큰_null로_주문_CART_ORDER_SESSION_INVALID() {
			Cart cart = CartFixture.CART_WITH_SESSION();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(cart));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderService.postOrderWithCart(receiptId, userPassport, null))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
				verify(orderWriter, never()).postOrderWithCart(any(), any());
			});
		}

		@Test
		void 세션_만료_후_주문_CART_ORDER_SESSION_EXPIRED() {
			Cart expiredCart = CartFixture.CART_WITH_EXPIRED_SESSION();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(expiredCart));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderService.postOrderWithCart(receiptId, userPassport, sessionToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_EXPIRED);
				verify(orderWriter, never()).postOrderWithCart(any(), any());
			});
		}

		@Test
		void 세션_없는_장바구니로_주문_CART_ORDER_SESSION_INVALID() {
			Cart cartWithoutSession = CartFixture.GENERAL_CART_SINGLE();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(cartWithoutSession));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderService.postOrderWithCart(receiptId, userPassport, sessionToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
				verify(orderWriter, never()).postOrderWithCart(any(), any());
			});
		}

		@Test
		void 유효한_세션_빈_장바구니로_주문_CART_EMPTY() {
			Cart emptyCart = CartFixture.EMPTY_CART_WITH_SESSION();
			given(receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId))
				.willReturn(Optional.of(receipt));
			given(cartWriter.getCartWithLock(receiptId)).willReturn(Optional.of(emptyCart));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderService.postOrderWithCart(receiptId, userPassport, sessionToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_EMPTY);
				verify(orderWriter, never()).postOrderWithCart(any(), any());
			});
		}
	}
}
