package domain.pos.cart.service;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.cart.entity.Cart;
import domain.pos.cart.implement.CartWriter;
import domain.pos.menu.implement.MenuReader;
import fixtures.cart.CartFixture;

class CartServiceTest extends ServiceTest {

	@Mock
	private MenuReader menuReader;

	@Mock
	private CartWriter cartWriter;

	@InjectMocks
	private CartService cartService;

	@Nested
	class upsertCartTest {
		private final UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		private final Long menuId = 1L;
		private final Integer quantity = 2;

		@Test
		void 성공() {
			given(cartWriter.isCartPending(receiptId)).willReturn(false);

			cartService.upsertCart(receiptId, menuId, quantity);

			verify(cartWriter).upsertCart(receiptId, menuId, quantity);
		}

		@Test
		void menuId가_유효하지_않은_경우() {
			given(cartWriter.isCartPending(receiptId)).willReturn(false);
			doThrow(IllegalArgumentException.class)
				.when(cartWriter).upsertCart(receiptId, menuId, quantity);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.upsertCart(receiptId, menuId, quantity))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(cartWriter).upsertCart(receiptId, menuId, quantity);
			});
		}

		@Test
		void 활성_세션_중_메뉴_추가_차단() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			given(cartWriter.isCartPending(receiptId)).willReturn(true);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.upsertCart(receiptId, 1L, 1))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_ACTIVE);
				verify(cartWriter, never()).upsertCart(any(), anyLong(), anyInt());
			});
		}

		@Test
		void 세션_만료_후_메뉴_추가_성공() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			given(cartWriter.isCartPending(receiptId)).willReturn(false);

			cartService.upsertCart(receiptId, 1L, 1);

			verify(cartWriter).upsertCart(receiptId, 1L, 1);
		}
	}

	@Nested
	@DisplayName("장바구니 삭제")
	class DeleteCartTest {

		@Test
		void 성공() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			Long menuId = 10L;
			given(cartWriter.isCartPending(receiptId)).willReturn(false);

			cartService.deleteCartMenu(receiptId, menuId);

			verify(cartWriter).deleteCartMenu(receiptId, menuId);
		}

		@Test
		void 활성_세션_중_메뉴_삭제_차단() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			Long menuId = 10L;
			given(cartWriter.isCartPending(receiptId)).willReturn(true);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.deleteCartMenu(receiptId, menuId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_ACTIVE);
				verify(cartWriter, never()).deleteCartMenu(any(), anyLong());
			});
		}
	}

	@Nested
	@DisplayName("장바구니 조회")
	class GetCartTest {

		@Test
		void 성공_장바구니존재() {
			Cart expected = CartFixture.GENERAL_CART_SINGLE();
			UUID receiptId = expected.getReceiptId();

			doReturn(Optional.of(expected))
				.when(cartWriter).getCart(receiptId);

			Optional<Cart> result = cartService.getCart(receiptId);

			assertSoftly(softly -> {
				softly.assertThat(result).isPresent()
					.contains(expected);
				verify(cartWriter).getCart(receiptId);
			});
		}

		@Test
		void 빈옵션_장바구니없음() {
			UUID receiptId = UUID.fromString("445e4567-e89b-12d3-a456-426614174000");
			doReturn(Optional.empty())
				.when(cartWriter).getCart(receiptId);

			Optional<Cart> result = cartService.getCart(receiptId);

			assertSoftly(softly -> {
				softly.assertThat(result).isEmpty();
				verify(cartWriter).getCart(receiptId);
			});
		}
	}

	@Nested
	@DisplayName("주문 대기 세션 진입")
	class EnterOrderSessionTest {

		@Test
		void 성공_세션_없을때_진입() {
			Cart expected = CartFixture.CART_WITH_SESSION();
			UUID receiptId = expected.getReceiptId();
			given(cartWriter.enterOrderSession(receiptId)).willReturn(expected);

			Cart result = cartService.enterOrderSession(receiptId);

			assertSoftly(softly -> {
				softly.assertThat(result.getSessionToken()).isNotNull();
				softly.assertThat(result.getPendingAt()).isNotNull();
				verify(cartWriter).enterOrderSession(receiptId);
			});
		}

		@Test
		void 장바구니_없을때_CART_NOT_FOUND() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			given(cartWriter.enterOrderSession(receiptId))
				.willThrow(new ServiceException(ErrorCode.CART_NOT_FOUND));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.enterOrderSession(receiptId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
			});
		}

		@Test
		void 이미_활성_세션이_있을때_CART_ORDER_SESSION_ACTIVE() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			given(cartWriter.enterOrderSession(receiptId))
				.willThrow(new ServiceException(ErrorCode.CART_ORDER_SESSION_ACTIVE));

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.enterOrderSession(receiptId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_ACTIVE);
			});
		}
	}

	@Nested
	@DisplayName("주문 대기 세션 취소")
	class CancelOrderSessionTest {

		@Test
		void 유효한_토큰으로_세션_취소_성공() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			UUID sessionToken = CartFixture.SESSION_TOKEN;
			willDoNothing().given(cartWriter).cancelOrderSession(receiptId, sessionToken);

			cartService.cancelOrderSession(receiptId, sessionToken);

			verify(cartWriter).cancelOrderSession(receiptId, sessionToken);
		}

		@Test
		void 잘못된_토큰으로_세션_취소_CART_ORDER_SESSION_INVALID() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			UUID wrongToken = UUID.randomUUID();
			doThrow(new ServiceException(ErrorCode.CART_ORDER_SESSION_INVALID))
				.when(cartWriter).cancelOrderSession(receiptId, wrongToken);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.cancelOrderSession(receiptId, wrongToken))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
			});
		}

		@Test
		void 세션_없는_상태에서_취소_CART_ORDER_SESSION_INVALID() {
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			UUID token = UUID.randomUUID();
			doThrow(new ServiceException(ErrorCode.CART_ORDER_SESSION_INVALID))
				.when(cartWriter).cancelOrderSession(receiptId, token);

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.cancelOrderSession(receiptId, token))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ORDER_SESSION_INVALID);
			});
		}
	}

}
