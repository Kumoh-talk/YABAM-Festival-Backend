package com.pos.cart.repository.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
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
	public void upsertCart(final UUID receiptId, final Long menuId, final Integer quantity) {
		MenuEntity menuEntity = menuJpaRepository.findById(menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptWithLock(receiptId)
			.orElseGet(() -> cartJpaRepository.save(CartEntity.from(ReceiptEntity.from(receiptId))));
		cartJpaRepository.findCartMenuByCartIdAndCartMenuWithLock(cartEntity, menuId)
			.ifPresentOrElse(cartMenuEntity -> cartMenuEntity.plusQuantity(quantity), () -> {
				CartMenuEntity cartMenuEntity = CartMenuEntity.of(cartEntity, menuEntity, quantity);
				cartEntity.getCartMenus().add(cartMenuEntity);
				cartMenuJpaRepository.save(cartMenuEntity);
			});
	}

	@Override
	@Transactional
	public void deleteCartMenu(final UUID receiptId, final Long menuId) {
		cartJpaRepository.deleteCartMenuByReceiptIdAndMenuId(receiptId, menuId);
	}

	@Override
	public Optional<Cart> getCart(UUID receiptId) {
		return cartJpaRepository.findCartByReceiptId(receiptId)
			.map(cartEntity -> CartMapper.toCart(receiptId, cartEntity));
	}

	@Override
	public Optional<Cart> getCartWithLock(UUID receiptId) {
		return cartJpaRepository.findCartByReceiptWithLock(receiptId)
			.map(cartEntity -> CartMapper.toCart(receiptId, cartEntity));
	}

	@Override
	public void deleteCartAndCartMenuByReceiptId(UUID receiptId) {
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptId(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CART_NOT_FOUND));
		cartMenuJpaRepository.deleteAll(cartEntity.getCartMenus());
		cartEntity.getCartMenus().clear();
		cartJpaRepository.delete(cartEntity);
	}

	@Override
	@Transactional
	public Cart enterOrderSession(UUID receiptId) {
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptWithLock(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CART_NOT_FOUND));

		if (cartEntity.getSessionToken() != null && cartEntity.getPendingAt() != null
			&& cartEntity.getPendingAt().plusSeconds(CartEntity.SESSION_TIMEOUT_SECONDS).isAfter(LocalDateTime.now())) {
			throw new ServiceException(ErrorCode.CART_ORDER_SESSION_ACTIVE);
		}

		cartEntity.startSession(UUID.randomUUID());

		return CartMapper.toCart(receiptId, cartEntity);
	}

	@Override
	@Transactional
	public void cancelOrderSession(UUID receiptId, UUID token) {
		CartEntity cartEntity = cartJpaRepository.findCartByReceiptWithLock(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CART_NOT_FOUND));

		if (cartEntity.getSessionToken() == null
			|| !cartEntity.getSessionToken().equals(token)
			|| cartEntity.getPendingAt() == null
			|| !cartEntity.getPendingAt().plusSeconds(CartEntity.SESSION_TIMEOUT_SECONDS).isAfter(LocalDateTime.now())) {
			throw new ServiceException(ErrorCode.CART_ORDER_SESSION_INVALID);
		}

		cartEntity.clearSession();
	}

	@Override
	public boolean isCartPending(UUID receiptId) {
		return cartJpaRepository.isCartPending(receiptId);
	}
}
