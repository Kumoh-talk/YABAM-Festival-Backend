package com.application.presentation.review.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.review.api.ReviewApi;
import com.application.presentation.review.dto.request.ReviewCreateRequest;
import com.application.presentation.review.dto.request.ReviewUpdateRequest;
import com.application.presentation.review.dto.response.ReviewIdResponse;
import com.application.presentation.review.dto.response.ReviewsCusorResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.review.entity.Review;
import domain.pos.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {
	private final ReviewService reviewService;

	/**
	 * @deprecated since 2025-05-15
	 * 기획상 보류.
	 */
	@Deprecated(since = "2025-05-15")
	@PostMapping("/api/v1/review")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<ReviewIdResponse>> createReview(
		UserPassport userPassport,
		@RequestBody @Valid ReviewCreateRequest reviewCreateRequest
	) {
		Review review = reviewService.postReview(userPassport, reviewCreateRequest.storeId(),
			reviewCreateRequest.receiptId(),
			reviewCreateRequest.getReviewInfo());
		return ResponseEntity.ok(createSuccessResponse(
			ReviewIdResponse.from(review)
		));
	}

	/**
	 * @deprecated since 2025-05-08
	 * 기획상 삭제될 예정.
	 */
	@Deprecated(since = "2025-05-08")
	@PatchMapping("/api/v1/review")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<ReviewIdResponse>> updateReview(
		UserPassport userPassport,
		@RequestBody @Valid ReviewUpdateRequest reviewUpdateRequest
	) {
		Review review = reviewService.updateReview(userPassport, reviewUpdateRequest.reviewId(),
			reviewUpdateRequest.getReviewInfo());
		return ResponseEntity.ok(createSuccessResponse(
			ReviewIdResponse.from(review)
		));
	}

	/**
	 * @deprecated since 2025-05-15
	 * 기획상 보류.
	 */
	@Deprecated(since = "2025-05-15")
	@DeleteMapping("/api/v1/review")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteReview(
		UserPassport userPassport,
		@RequestParam Long reviewId
	) {
		reviewService.deleteReview(userPassport, reviewId);
		return ResponseEntity.ok(createSuccessResponse());
	}

	/**
	 * @deprecated since 2025-05-15
	 * 기획상 보류.
	 */
	@Deprecated(since = "2025-05-15")
	@GetMapping("/api/v1/reviews")
	public ResponseEntity<ResponseBody<ReviewsCusorResponse>> getReview(
		@RequestParam Long storeId,
		@RequestParam(required = false) Long lastReviewId,
		@RequestParam int size
	) {
		return ResponseEntity.ok(createSuccessResponse(
			ReviewsCusorResponse.from(reviewService.getReviews(
				storeId, lastReviewId, size))
		));
	}
}
