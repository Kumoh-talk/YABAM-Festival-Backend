package com.application.presentation.review.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.review.dto.request.ReviewCreateRequest;
import com.application.presentation.review.dto.request.ReviewCusorRequest;
import com.application.presentation.review.dto.request.ReviewUpdateRequest;
import com.application.presentation.review.dto.response.ReviewIdResponse;
import com.application.presentation.review.dto.response.ReviewsCusorResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@Schema(description = "리뷰 API")
public interface ReviewApi {

	@Operation(
		summary = "리뷰 작성 API",
		description = "리뷰를 작성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReviewIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReviewIdResponse.class,
			description = "리뷰 작성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_NOT_ADJUSTMENT),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_ALREADY_EXITS),
		}
	)
	ResponseEntity<ResponseBody<ReviewIdResponse>> createReview(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestBody @Valid ReviewCreateRequest reviewCreateRequest);

	@Operation(
		summary = "리뷰 수정 API",
		description = "리뷰를 수정합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReviewIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReviewIdResponse.class,
			description = "리뷰 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_NOT_USER)
		}
	)
	ResponseEntity<ResponseBody<ReviewIdResponse>> updateReview(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestBody @Valid ReviewUpdateRequest reviewUpdateRequest);

	@Operation(
		summary = "리뷰 삭제 API",
		description = "리뷰를 삭제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "리뷰 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.REVIEW_NOT_USER)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteReview(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestParam Long reviewId);

	@Operation(
		summary = "리뷰 목록 조회 API",
		description = "리뷰를 커서 기반으로 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReviewsCusorResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReviewsCusorResponse.class,
			description = "리뷰 목록 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<ReviewsCusorResponse>> getReview(
		@RequestBody @Valid ReviewCusorRequest reviewCusorRequest);
}
