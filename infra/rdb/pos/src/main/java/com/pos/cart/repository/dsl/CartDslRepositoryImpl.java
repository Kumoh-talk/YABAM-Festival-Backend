package com.pos.cart.repository.dsl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pos.cart.entity.CartEntity;
import com.pos.cart.entity.CartMenuEntity;
import com.pos.cart.entity.QCartEntity;
import com.pos.cart.entity.QCartMenuEntity;
import com.pos.menu.entity.QMenuEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartDslRepositoryImpl implements CartDslRepository {
	private final JPAQueryFactory queryFactory;

	private final QCartEntity qCartEntity = QCartEntity.cartEntity;
	private final QCartMenuEntity qCartMenuEntity = QCartMenuEntity.cartMenuEntity;
	private final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;

	@Override
	public Optional<CartEntity> findCartByReceiptWithLock(Long receiptId) {
		return Optional.ofNullable(queryFactory
			.selectFrom(qCartEntity)
			.from(qCartEntity)
			.where(qCartEntity.receipt.id.eq(receiptId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne());
	}

	public Optional<CartMenuEntity> findCartMenuByCartIdAndCartMenuWithLock(CartEntity cartEntity, Long menuId) {
		return Optional.ofNullable(queryFactory
			.selectFrom(qCartMenuEntity)
			.from(qCartMenuEntity)
			.where(qCartMenuEntity.cart.id.eq(cartEntity.getId())
				.and(qCartMenuEntity.menu.id.eq(menuId)))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne());
	}

	public void deleteCartMenuByReceiptIdAndMenuId(final Long receiptId, final Long menuId) {
		queryFactory
			.delete(qCartMenuEntity)
			.where(isEqualCartAndReceipt(receiptId)
				.and(qCartMenuEntity.menu.id.eq(menuId)))
			.execute();
	}

	private BooleanExpression isEqualCartAndReceipt(Long receiptId) {
		return qCartMenuEntity.cart.id.eq(queryFactory.select(qCartEntity.id)
			.from(qCartEntity)
			.where(qCartEntity.receipt.id.eq(receiptId))
			.fetchOne());
	}

	public Optional<CartEntity> findCartByReceiptId(Long receiptId) {
		return Optional.ofNullable(queryFactory
			.selectFrom(qCartEntity)
			.join(qCartEntity.cartMenus, qCartMenuEntity).fetchJoin()
			.join(qCartMenuEntity.menu, qMenuEntity).fetchJoin()
			.where(qCartEntity.receipt.id.eq(receiptId))
			.fetchFirst());
	}

}
