package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;

import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.entity.QMenuCategoryEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MenuCategoryQueryDslRepositoryImpl implements MenuCategoryQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QMenuCategoryEntity qMenuCategoryEntity = QMenuCategoryEntity.menuCategoryEntity;

	@Override
	public Optional<MenuCategoryEntity> findByIdAndStoreId(Long menuCategoryId, Long storeId) {
		MenuCategoryEntity menuCategoryEntity = jpaQueryFactory.selectFrom(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.id.eq(menuCategoryId)
				.and(qMenuCategoryEntity.store.id.eq(storeId)))
			.fetchOne();

		return Optional.ofNullable(menuCategoryEntity);
	}

	@Override
	public List<MenuCategoryEntity> findAllByStoreId(Long storeId) {
		return jpaQueryFactory
			.selectFrom(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.store.id.eq(storeId))
			.orderBy(qMenuCategoryEntity.order.asc())
			.fetch();
	}

	@Override
	public List<MenuCategoryEntity> findAllByStoreIdWithLock(Long storeId) {
		return jpaQueryFactory
			.selectFrom(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.store.id.eq(storeId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetch();
	}

	@Override
	public Optional<Integer> findMaxOrderByStoreId(Long storeId) {
		Integer maxOrder = jpaQueryFactory
			.select(qMenuCategoryEntity.order.max())
			.from(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.store.id.eq(storeId))
			.fetchOne();

		return Optional.ofNullable(maxOrder);
	}

	@Override
	public boolean existsByIdAndStoreId(Long menuCategoryId, Long storeId) {
		return jpaQueryFactory
			.selectOne()
			.from(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.id.eq(menuCategoryId)
				.and(qMenuCategoryEntity.store.id.eq(storeId)))
			.fetchFirst() != null;
	}

	@Override
	public boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder) {
		return jpaQueryFactory
			.selectOne()
			.from(qMenuCategoryEntity)
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.eq(menuCategoryOrder)))
			.fetchFirst() != null;
	}

	@Override
	public void decreaseOrderInRange(Long storeId, Integer startOrder, Integer endOrder) {
		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.subtract(100))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.gt(startOrder))
				.and(qMenuCategoryEntity.order.loe(endOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.add(99))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.gt(startOrder - 100))
				.and(qMenuCategoryEntity.order.loe(endOrder - 100)))
			.execute();
	}

	@Override
	public void increaseOrderInRange(Long storeId, Integer startOrder, Integer endOrder) {
		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.subtract(100))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.goe(startOrder))
				.and(qMenuCategoryEntity.order.lt(endOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.add(101))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.goe(startOrder - 100))
				.and(qMenuCategoryEntity.order.lt(endOrder - 100)))
			.execute();
	}

	@Override
	public void decreaseOrderWhereGT(Long storeId, Integer deleteOrder) {
		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.subtract(100))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.gt(deleteOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuCategoryEntity)
			.set(qMenuCategoryEntity.order, qMenuCategoryEntity.order.add(99))
			.where(qMenuCategoryEntity.store.id.eq(storeId)
				.and(qMenuCategoryEntity.order.gt(deleteOrder - 100))
				.and(qMenuCategoryEntity.order.loe(0)))
			.execute();
	}
}
