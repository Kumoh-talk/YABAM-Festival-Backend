package domain.pos.cart.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.repository.CartRepository;
import domain.pos.menu.entity.MenuInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartWriter {
	private final CartRepository cartRepository;

	public Cart upsertCart(final Long receiptId, final MenuInfo menuInfo, final Integer quantity) {
		return cartRepository.postCart(receiptId, menuInfo, quantity);
	}

	public void deleteCartMenu(Long receiptId, Long menuId) {
		cartRepository.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(Long receiptId) {
		return cartRepository.getCart(receiptId);
	}
}
