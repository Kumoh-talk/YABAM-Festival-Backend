package com.pos.cart.repository.dsl;

import java.util.Optional;
import java.util.UUID;

import com.pos.cart.entity.CartEntity;
import com.pos.cart.entity.CartMenuEntity;

public interface CartDslRepository {
	Optional<CartEntity> findCartByReceiptWithLock(UUID receiptId);

	Optional<CartMenuEntity> findCartMenuByCartIdAndCartMenuWithLock(CartEntity cartEntity, Long menuId);

	void deleteCartMenuByReceiptIdAndMenuId(UUID receiptId, Long menuId);

	Optional<CartEntity> findCartByReceiptId(UUID receiptId);
}
