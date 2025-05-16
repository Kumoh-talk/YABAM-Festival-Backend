package com.pos.store.repository.dsl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.pos.review.entity.QReviewEntity;
import com.pos.store.entity.QStoreDetailImageEntity;
import com.pos.store.entity.QStoreEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.store.entity.dto.StoreHeadDto;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreDslRepositoryImpl implements StoreDslRepository {

	private final JPAQueryFactory queryFactory;

	private final QStoreEntity store = QStoreEntity.storeEntity;
	private final QReviewEntity review = QReviewEntity.reviewEntity;
	private final QStoreDetailImageEntity storeDetailImage = QStoreDetailImageEntity.storeDetailImageEntity;

	@Override
	public Slice<StoreHeadDto> findStoreHeadsByStoreIdCursor(
		Long lastStoreId, int size) {
		List<StoreEntity> result = queryFactory
			.select(store)
			.from(store)
			.leftJoin(store.storeDetailImageEntity, storeDetailImage).fetchJoin()
			.where(cursorCondition(lastStoreId))
			.orderBy(store.id.desc())
			.limit(size + 1)
			.fetch();
		boolean hasNext = result.size() > size;
		if (hasNext) {
			result.remove(size);
		}
		List<StoreHeadDto> list = result.stream()
			.map(StoreMapper::toStoreHeadDto)
			.toList();
		return new SliceImpl<>(list, PageRequest.of(0, size), hasNext);

	}

	private BooleanExpression cursorCondition(Long lastStoreId) {
		if (lastStoreId == null) {
			return null;
		}
		return store.id.lt(lastStoreId);
	}

}
