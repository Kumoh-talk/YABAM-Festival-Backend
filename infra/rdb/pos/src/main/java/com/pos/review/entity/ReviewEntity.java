package com.pos.review.entity;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.vo.ReviewUser;

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
public class ReviewEntity {
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
	@JoinColumn(name = "receipt_id")
	private ReceiptEntity receipt;

	private ReviewEntity(ReviewUser reviewUser, String content, Integer rating, ReceiptEntity receipt) {
		this.reviewUser = reviewUser;
		this.content = content;
		this.rating = rating;
		this.receipt = receipt;
	}

	public static ReviewEntity of(ReviewUser reviewUser, String content, Integer rating, ReceiptEntity receipt) {
		return new ReviewEntity(reviewUser, content, rating, receipt);
	}

}
