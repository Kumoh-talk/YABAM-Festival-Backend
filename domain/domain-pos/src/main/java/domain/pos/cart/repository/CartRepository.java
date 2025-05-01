package domain.pos.cart.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.cart.entity.Cart;
import domain.pos.menu.entity.MenuInfo;

@Repository
public interface CartRepository {
	Cart postCart(Long receiptId, MenuInfo menuInfo, Integer quantity);

	void deleteCartMenu(Long receiptId, Long menuId);

	Optional<Cart> getCart(Long receiptId);
}
