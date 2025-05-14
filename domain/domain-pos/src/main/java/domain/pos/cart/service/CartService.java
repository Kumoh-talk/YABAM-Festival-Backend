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
		try {
			cartWriter.upsertCart(receiptId, menuId, quantity);
		} catch (IllegalArgumentException e) {
			log.warn(e.getMessage());
			throw new ServiceException(ErrorCode.MENU_NOT_FOUND);
		}
	}

	public void deleteCartMenu(final UUID receiptId, final Long menuId) {
		cartWriter.deleteCartMenu(receiptId, menuId);
	}

	public Optional<Cart> getCart(final UUID receiptId) {
		return cartWriter.getCart(receiptId);
	}
}
