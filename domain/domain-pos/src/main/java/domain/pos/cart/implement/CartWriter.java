package domain.pos.cart.implement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartWriter {
	private final CartRepository cartRepository;

	public void upsertCart(final UUID receiptId, final Long menuId, final Integer quantity) {
		cartRepository.upsertCart(receiptId, menuId, quantity);
	}

	public void deleteCartMenu(UUID receiptId, Long menuId) {
		cartRepository.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(UUID receiptId) {
		return cartRepository.getCart(receiptId);
	}

	public Optional<Cart> getCartWithLock(UUID receiptId) {
		return cartRepository.getCartWithLock(receiptId);
	}

	public void deleteCartAndCartMenuByReceiptId(UUID receiptId) {
		cartRepository.deleteCartAndCartMenuByReceiptId(receiptId);
	}

	public Cart enterOrderSession(UUID receiptId) {
		return cartRepository.enterOrderSession(receiptId);
	}

	public void cancelOrderSession(UUID receiptId, UUID token) {
		cartRepository.cancelOrderSession(receiptId, token);
	}

	public boolean isCartPending(UUID receiptId) {
		return cartRepository.isCartPending(receiptId);
	}
}
