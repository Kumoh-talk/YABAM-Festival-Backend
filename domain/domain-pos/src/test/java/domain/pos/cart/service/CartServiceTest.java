package domain.pos.cart.service;

import static org.assertj.core.api.SoftAssertions.*;
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
		@Test
		void 성공() {
			// when
			cartService.upsertCart(any(), anyLong(), anyInt());

			// then
			assertSoftly(softly -> {
				verify(cartWriter).upsertCart(any(), anyLong(), anyInt());
			});
		}

		@Test
		void menuId가_유효하지_않은_경우() {
			// given
			doThrow(IllegalArgumentException.class)
				.when(cartWriter).upsertCart(any(), anyLong(), anyInt());

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> cartService.upsertCart(any(), anyLong(), anyInt()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
				verify(cartWriter).upsertCart(any(), anyLong(), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("장바구니 삭제")
	class DeleteCartTest {

		@Test
		void 성공() {
			// given
			UUID receiptId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			Long menuId = 10L;

			// when
			cartService.deleteCartMenu(receiptId, menuId);

			// then
			assertSoftly(softly -> {
				verify(cartWriter).deleteCartMenu(receiptId, menuId);
				// 반환값 없으므로 추가 assert 필요 없음
			});
		}
	}

	@Nested
	@DisplayName("장바구니 조회")
	class GetCartTest {

		@Test
		void 성공_장바구니존재() {
			// given
			Cart expected = CartFixture.GENERAL_CART_SINGLE();
			UUID receiptId = expected.getReceiptId();

			doReturn(Optional.of(expected))
				.when(cartWriter).getCart(receiptId);

			// when
			Optional<Cart> result = cartService.getCart(receiptId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isPresent()
					.contains(expected);
				verify(cartWriter).getCart(receiptId);
			});
		}

		@Test
		void 빈옵션_장바구니없음() {
			// given
			UUID receiptId = UUID.fromString("445e4567-e89b-12d3-a456-426614174000");
			doReturn(Optional.empty())
				.when(cartWriter).getCart(receiptId);

			// when
			Optional<Cart> result = cartService.getCart(receiptId);

			// then
			assertSoftly(softly -> {
				softly.assertThat(result).isEmpty();
				verify(cartWriter).getCart(receiptId);
			});
		}
	}

}
