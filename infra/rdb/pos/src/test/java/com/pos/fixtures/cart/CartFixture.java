package com.pos.fixtures.cart;

import com.pos.cart.entity.CartEntity;
import com.pos.receipt.entity.ReceiptEntity;

public class CartFixture {
	public static CartEntity CART_ENTITY(ReceiptEntity receiptEntity) {
		return CartEntity.from(receiptEntity);
	}
}
