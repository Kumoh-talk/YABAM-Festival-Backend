package com.pos.store.repository.dsl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.pos.review.entity.QReviewEntity;
import com.pos.store.entity.QStoreDetailImageEntity;
import com.pos.store.entity.QStoreEntity;
import com.querydsl.core.Tuple;
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
	public Slice<StoreHeadDto> findStoreHeadsByReviewCountCursor(
		Long cursorReviewCnt, Long cursorStoreId, int size) {
		List<Tuple> results = queryFactory
			.select(
				store.id,
				store.name,
				store.isOpen,
				store.headImageUrl,
				store.description,
				review.id.countDistinct(),
				review.rating.avg()
			)
			.from(review)
			.rightJoin(review.store, store)
			.groupBy(store.id)
			.having(cursorReviewCountOrCursorStoreIdLt(cursorReviewCnt, cursorStoreId))
			.orderBy(review.id.countDistinct().desc(), store.id.desc())
			.limit(size + 1)
			.fetch();
		return tupleToStoreHeadDto(results, size);
	}

	private BooleanExpression cursorReviewCountOrCursorStoreIdLt(Long cursorReviewCnt, Long cursorStoreId) {
		if (cursorReviewCnt == null || cursorStoreId == null) {
			return null;
		}
		return review.id.countDistinct().lt(cursorReviewCnt)
			.or(review.id.countDistinct().eq(cursorReviewCnt)
				.and(store.id.lt(cursorStoreId)));
	}

	private Slice<StoreHeadDto> tupleToStoreHeadDto(List<Tuple> results, int size) {
		boolean hasNext = results.size() > size;
		if (hasNext) {
			results.remove(size);
		}
		List<Long> storeIds = results.stream()
			.map(t -> t.get(store.id))
			.toList();

		if (storeIds.isEmpty()) {
			return new SliceImpl<>(List.of(), PageRequest.of(0, size), hasNext);
		}
		Map<Long, List<String>> imageUrlMap = getStoreImageUrlsByStoreIds(storeIds);

		List<StoreHeadDto> list = results.stream()
			.map(tuple -> {
				int ratingAvg = tuple.get(review.rating.avg()) == null ? 0 : tuple.get(review.rating.avg()).intValue();
				return StoreHeadDto.of(
					tuple.get(store.id),
					tuple.get(store.name),
					tuple.get(store.isOpen),
					tuple.get(store.headImageUrl),
					tuple.get(store.description),
					ratingAvg,
					tuple.get(review.id.countDistinct()).intValue(),
					imageUrlMap.get(tuple.get(store.id)) == null
						? List.of()
						: imageUrlMap.get(tuple.get(store.id))
				);
			})
			.toList();
		return new SliceImpl<>(list, PageRequest.of(0, size), hasNext);

	}

	private Map<Long, List<String>> getStoreImageUrlsByStoreIds(List<Long> storeIds) {
		return queryFactory
			.select(storeDetailImage.store.id, storeDetailImage.imageUrl)
			.from(storeDetailImage)
			.where(storeDetailImage.store.id.in(storeIds))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(
				t -> t.get(storeDetailImage.store.id),
				Collectors.mapping(t -> t.get(storeDetailImage.imageUrl), Collectors.toList())
			));
	}
}
