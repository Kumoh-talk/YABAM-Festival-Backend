package com.pos.review.repository.dsl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.pos.review.entity.QReviewEntity;
import com.pos.review.entity.ReviewEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewDslRepositoryImpl implements ReviewDslRepository {
	private final JPAQueryFactory queryFactory;

	private final QReviewEntity qReviewEntity = QReviewEntity.reviewEntity;

	@Override
	public boolean existsByReceiptIdAndUserId(Long receiptId, Long userId) {
		return queryFactory
			.select(qReviewEntity.count())
			.from(qReviewEntity)
			.where(qReviewEntity.receipt.id.eq(receiptId)
				.and(qReviewEntity.reviewUser.userId.eq(userId)))
			.fetchOne() > 0;
	}

	@Override
	public Slice<ReviewEntity> findReviewsWithUser(Long receiptId, Long lastReviewId, int size) {
		List<ReviewEntity> results = queryFactory
			.selectFrom(qReviewEntity)
			.where(qReviewEntity.id.lt(lastReviewId)
				.and(qReviewEntity.receipt.id.eq(receiptId)))
			.orderBy(qReviewEntity.id.desc())
			.limit(size + 1)
			.fetch();
		boolean hasNext = results.size() > size;
		if (hasNext) {
			results.remove(size);
		}

		return new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
	}

}
