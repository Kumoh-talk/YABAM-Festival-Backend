package domain.pos.cart.service;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import base.ServiceTest;
import domain.pos.cart.entity.Cart;
import domain.pos.cart.implement.CartWriter;
import domain.pos.menu.entity.MenuInfo;
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
			// given
			final Cart responCart = CartFixture.GENERAL_CART_SINGLE();
			final Long receiptId = responCart.getReceiptId();
			final Long menuId = responCart.getCartMenus().get(0).getMenuInfo().getId();
			final Integer quantity = responCart.getCartMenus().get(0).getQuantity();

			doReturn(responCart.getCartMenus().get(0).getMenuInfo())
				.when(menuReader).getMenuInfoById(anyLong());
			doReturn(responCart)
				.when(cartWriter).upsertCart(anyLong(), any(MenuInfo.class), anyInt());
			// when
			final Cart cart = cartService.upsertCart(receiptId, menuId, quantity);

			// then
			assertSoftly(softly -> {
				verify(menuReader).getMenuInfoById(anyLong());
				verify(cartWriter).upsertCart(anyLong(), any(MenuInfo.class), anyInt());
			});
		}
	}

	@Nested
	@DisplayName("장바구니 삭제")
	class DeleteCartTest {

		@Test
		void 성공() {
			// given
			Long receiptId = 1L;
			Long menuId = 10L;

			// when
			cartService.deleteCart(receiptId, menuId);

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
			Long receiptId = expected.getReceiptId();

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
			Long receiptId = 404L;
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
