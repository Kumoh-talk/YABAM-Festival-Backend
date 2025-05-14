package com.pos.menu.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.pos.menu.entity.MenuEntity;
import com.pos.menu.entity.QMenuEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MenuQueryDslRepositoryImpl implements MenuQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;

	@Override
	public Optional<MenuEntity> findByIdAndStoreId(Long menuId, Long storeId) {
		MenuEntity menuEntity = jpaQueryFactory
			.selectFrom(qMenuEntity)
			.where(qMenuEntity.id.eq(menuId)
				.and(qMenuEntity.store.id.eq(storeId)))
			.fetchOne();

		return Optional.ofNullable(menuEntity);
	}

	@Override
	public List<MenuEntity> findAllByStoreIdWithCategoryAndLock(Long storeId) {
		return jpaQueryFactory
			.selectFrom(qMenuEntity)
			.join(qMenuEntity.menuCategory).fetchJoin()
			.where(qMenuEntity.store.id.eq(storeId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetch();

	}

	@Override
	public Slice<MenuEntity> findSliceByMenuCategoryId(int pageSize, Integer lastMenuOrder, Long menuCategoryId) {
		List<MenuEntity> content = jpaQueryFactory
			.selectFrom(qMenuEntity)
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(lastMenuOrder == null ? Expressions.TRUE : qMenuEntity.order.gt(lastMenuOrder))
			)
			.orderBy(qMenuEntity.order.asc())
			.limit(pageSize + 1)
			.fetch();

		boolean hasNext = content.size() > pageSize;
		if (hasNext) {
			content.remove(pageSize);
		}

		return new SliceImpl<>(content, Pageable.ofSize(pageSize), hasNext);
	}

	@Override
	public Optional<Integer> findMaxOrderByMenuCategoryId(Long menuCategoryId) {
		Integer maxOrder = jpaQueryFactory
			.select(qMenuEntity.order.max())
			.from(qMenuEntity)
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId))
			.fetchOne();

		return Optional.ofNullable(maxOrder);
	}

	@Override
	public boolean existsByIdAndStoreId(Long menuId, Long storeId) {
		return jpaQueryFactory
			.selectOne()
			.from(qMenuEntity)
			.where(qMenuEntity.id.eq(menuId)
				.and(qMenuEntity.store.id.eq(storeId)))
			.fetchFirst() != null;
	}

	@Override
	public boolean existsMenuOrder(Long menuCategoryId, int menuOrder) {
		return jpaQueryFactory
			.selectOne()
			.from(qMenuEntity)
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.eq(menuOrder)))
			.fetchFirst() != null;
	}

	@Override
	public void decreaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder) {
		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.subtract(100))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.gt(startOrder))
				.and(qMenuEntity.order.loe(endOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.add(99))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.gt(startOrder - 100))
				.and(qMenuEntity.order.loe(endOrder - 100)))
			.execute();
	}

	@Override
	public void increaseOrderInRange(Long menuCategoryId, Integer startOrder, Integer endOrder) {
		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.subtract(100))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.goe(startOrder))
				.and(qMenuEntity.order.lt(endOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.add(101))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.goe(startOrder - 100))
				.and(qMenuEntity.order.lt(endOrder - 100)))
			.execute();
	}

	@Override
	public void decreaseOrderWhereGT(Long menuCategoryId, Integer deleteOrder) {
		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.subtract(100))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.gt(deleteOrder)))
			.execute();

		jpaQueryFactory
			.update(qMenuEntity)
			.set(qMenuEntity.order, qMenuEntity.order.add(99))
			.where(qMenuEntity.menuCategory.id.eq(menuCategoryId)
				.and(qMenuEntity.order.gt(deleteOrder - 100))
				.and(qMenuEntity.order.loe(0)))
			.execute();
	}

	@Override
	public Long countByIdIn(Long storeId, Set<Long> menuIds) {
		return jpaQueryFactory
			.select(qMenuEntity.count())
			.from(qMenuEntity)
			.where(
				qMenuEntity.store.id.eq(storeId)
					.and(qMenuEntity.id.in(menuIds))
			)
			.fetchOne();
	}
}
