package domain.pos.cart.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.implement.CartWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
	private final CartWriter cartWriter;

	public void upsertCart(final UUID receiptId, final Long menuId, final Integer quantity) {
		if (cartWriter.isCartPending(receiptId)) {
			throw new ServiceException(ErrorCode.CART_ORDER_SESSION_ACTIVE);
		}
		try {
			cartWriter.upsertCart(receiptId, menuId, quantity);
		} catch (IllegalArgumentException e) {
			log.warn(e.getMessage());
			throw new ServiceException(ErrorCode.MENU_NOT_FOUND);
		}
	}

	public void deleteCartMenu(final UUID receiptId, final Long menuId) {
		if (cartWriter.isCartPending(receiptId)) {
			throw new ServiceException(ErrorCode.CART_ORDER_SESSION_ACTIVE);
		}
		cartWriter.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(final UUID receiptId) {
		return cartWriter.getCart(receiptId);
	}

	public Cart enterOrderSession(final UUID receiptId) {
		return cartWriter.enterOrderSession(receiptId);
	}

	public void cancelOrderSession(final UUID receiptId, final UUID token) {
		cartWriter.cancelOrderSession(receiptId, token);
	}
}
