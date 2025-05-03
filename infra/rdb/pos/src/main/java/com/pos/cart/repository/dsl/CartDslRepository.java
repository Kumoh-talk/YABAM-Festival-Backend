package com.pos.cart.repository.dsl;

import java.util.Optional;

import com.pos.cart.entity.CartEntity;
import com.pos.cart.entity.CartMenuEntity;

public interface CartDslRepository {
	Optional<CartEntity> findCartByReceiptWithLock(Long receiptId);

	Optional<CartMenuEntity> findCartMenuByCartIdAndCartMenuWithLock(CartEntity cartEntity, Long menuId);

	void deleteCartMenuByReceiptIdAndMenuId(Long receiptId, Long menuId);

	Optional<CartEntity> findCartByReceiptId(Long receiptId);
}
