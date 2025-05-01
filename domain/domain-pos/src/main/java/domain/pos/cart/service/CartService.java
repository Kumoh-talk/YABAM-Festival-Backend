package domain.pos.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.implement.CartWriter;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
	private final MenuReader menuReader;
	private final CartWriter cartWriter;

	public Cart upsertCart(final Long receiptId, final Long menuId, final Integer quantity) {
		final MenuInfo menuInfo = menuReader.getMenuInfoById(menuId);

		return cartWriter.upsertCart(receiptId, menuInfo, quantity);
	}

	public void deleteCart(final Long receiptId, final Long menuId) {
		cartWriter.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(final Long receiptId) {
		return cartWriter.getCart(receiptId);
	}
}
