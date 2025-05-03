package com.pos.cart.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.cart.entity.CartEntity;
import com.pos.cart.entity.CartMenuEntity;
import com.pos.cart.mapper.CartMapper;
import com.pos.cart.repository.CartJpaRepository;
import com.pos.cart.repository.CartMenuJpaRepository;
import com.pos.menu.entity.MenuEntity;
import com.pos.menu.repository.jpa.MenuJpaRepository;
import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {
	private final CartMenuJpaRepository cartMenuJpaRepository;
	private final CartJpaRepository cartJpaRepository;
	private final MenuJpaRepository menuJpaRepository;

	@Override
	@Transactional
	public void upsertCart(final Long receiptId, final Long menuId, final Integer quantity) {
		MenuEntity menuEntity = menuJpaRepository.findById(menuId)
			.orElseThrow(() -> new IllegalArgumentException("해당 메뉴 id는 유효하지 않습니다." + menuId));
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptWithLock(receiptId)
			.orElseGet(() -> cartJpaRepository.save(CartEntity.from(ReceiptEntity.from(receiptId))));
		cartJpaRepository.findCartMenuByCartIdAndCartMenuWithLock(cartEntity, menuId)
			.ifPresentOrElse(cartMenuEntity -> {
				cartMenuEntity.plusQuantity(quantity);
			}, () -> {
				CartMenuEntity cartMenuEntity = CartMenuEntity.of(cartEntity, menuEntity, quantity);
				cartEntity.getCartMenus().add(cartMenuEntity);
				cartMenuJpaRepository.save(cartMenuEntity);
			});
	}

	@Override
	@Transactional
	public void deleteCartMenu(final Long receiptId, final Long menuId) {
		cartJpaRepository.deleteCartMenuByReceiptIdAndMenuId(receiptId, menuId);
	}

	@Override
	public Optional<Cart> getCart(Long receiptId) {
		return cartJpaRepository.findCartByReceiptId(receiptId)
			.map(cartEntity -> {
				return CartMapper.toCart(receiptId, cartEntity);
			});
	}

	@Override
	public void deleteCartAndCartMenuByReceiptId(Long receiptId) {
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptId(receiptId).orElseThrow(
			() -> new IllegalArgumentException("해당 영수증 id에 해당하는 장바구니 내역이 없습니다." + receiptId));
		cartJpaRepository.delete(cartEntity);
		cartMenuJpaRepository.deleteAll(cartEntity.getCartMenus());
		cartEntity.getCartMenus().clear();
	}
}
