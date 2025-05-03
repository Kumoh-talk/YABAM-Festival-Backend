package domain.pos.cart.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartWriter {
	private final CartRepository cartRepository;

	public void upsertCart(final Long receiptId, final Long menuId, final Integer quantity) {
		cartRepository.upsertCart(receiptId, menuId, quantity);
	}

	public void deleteCartMenu(Long receiptId, Long menuId) {
		cartRepository.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(Long receiptId) {
		return cartRepository.getCart(receiptId);
	}
}
