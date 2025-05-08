package com.pos.review.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.vo.ReviewUser;
import com.pos.store.entity.StoreEntity;

import domain.pos.review.entity.ReviewInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "reviews")
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE reviews SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
public class ReviewEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private ReviewUser reviewUser;

	@Column(nullable = true)
	private String content;

	@Column(nullable = false)
	private Integer rating;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receipt_id", nullable = false)
	private ReceiptEntity receipt;

	private ReviewEntity(ReviewUser reviewUser, String content, Integer rating, StoreEntity store,
		ReceiptEntity receipt) {
		this.reviewUser = reviewUser;
		this.content = content;
		this.rating = rating;
		this.store = store;
		this.receipt = receipt;
	}

	public static ReviewEntity of(ReviewUser reviewUser, String content, Integer rating, StoreEntity store,
		ReceiptEntity receipt) {
		return new ReviewEntity(reviewUser, content, rating, store, receipt);
	}

	public void changeReviewInfo(ReviewInfo updateReviewInfo) {
		this.content = updateReviewInfo.getContent();
		this.rating = updateReviewInfo.getRating();
	}
}
