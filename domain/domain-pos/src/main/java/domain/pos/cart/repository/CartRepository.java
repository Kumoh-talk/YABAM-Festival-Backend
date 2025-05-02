package domain.pos.cart.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.cart.entity.Cart;

@Repository
public interface CartRepository {
	// 해당 영수증에 장바구니가 없다면, 장바구니를 생성한다.
	// 장바구니가 있다면, 장바구니를 quantity 만큼 증가한다.
	void upsertCart(Long receiptId, Long menuId, Integer quantity);

	void deleteCartMenu(Long receiptId, Long menuId);

	Optional<Cart> getCart(Long receiptId);
}
