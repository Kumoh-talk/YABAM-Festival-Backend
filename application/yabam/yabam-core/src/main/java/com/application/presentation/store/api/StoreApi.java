package com.application.presentation.store.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.store.dto.request.StoreWriteRequest;
import com.application.presentation.store.dto.response.MyStoreResopnse;
import com.application.presentation.store.dto.response.StoreCursorResponse;
import com.application.presentation.store.dto.response.StoreIdResponse;
import com.application.presentation.store.dto.response.StoreInfoResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@Schema(description = "가게 API")
public interface StoreApi {

	@Operation(
		summary = "가게 생성 API",
		description = "가게를 생성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = StoreIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = StoreIdResponse.class,
			description = "가게 생성 성공"
		)
	)
	ResponseEntity<ResponseBody<StoreIdResponse>> createStore(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestBody @Valid final StoreWriteRequest storeWriteRequest);

	@Operation(
		summary = "가게 조회 API",
		description = "가게를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = StoreInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = StoreInfoResponse.class,
			description = "가게 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<StoreInfoResponse>> getStoreInfo(
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam final Long storeId);

	@Operation(
		summary = "가게 수정 API",
		description = "가게를 수정합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = StoreIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = StoreIdResponse.class,
			description = "가게 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER)
		}
	)
	ResponseEntity<ResponseBody<StoreIdResponse>> updateStoreInfo(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam final Long storeId,
		@RequestBody @Valid final StoreWriteRequest storeWriteRequest);

	@Operation(
		summary = "가게 삭제 API",
		description = "가게를 삭제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "가게 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteStore(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestParam final Long storeId);

	@Operation(
		summary = "가게 상세 이미지 업로드 API",
		description = "가게의 상세 이미지를 업로드합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "가게 이미지 업로드 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER)
		}
	)
	ResponseEntity<ResponseBody<Void>> uploadStoreImage(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam final Long storeId,
		@Schema(description = "이미지 URL", example = "https://example.com/image.jpg") @RequestParam final String detailImageUrl);

	@Operation(
		summary = "가게 상세 이미지 삭제 API",
		description = "가게의 상세 이미지를 삭제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "가게 이미지 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE_IMAGE)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteStoreImage(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam final Long storeId,
		@Schema(description = "삭제할 이미지 URL", example = "https://example.com/image.jpg") @RequestParam final String detailImageUrl);

	@Operation(
		summary = "가게 목록 커서 조회 API",
		description = "리뷰 수 DESC 기준으로 가게 리스트를 무한 스크롤 방식으로 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = StoreCursorResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "가게 커서 목록 조회 성공",
			responseClass = StoreCursorResponse.class
		)
	)
	ResponseEntity<ResponseBody<StoreCursorResponse>> getStoreList(
		@Schema(description = "마지막 가게 ID 만약 첫 조회면 null", example = "1")
		Long lastStoreId,
		@Schema(description = "가게 개수", example = "10")
		Integer size
	);

	@Operation(
		summary = "내가 등록한 가게 목록 조회 API",
		description = "점주가 등록한 모든 가게 목록을 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MyStoreResopnse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MyStoreResopnse.class,
			description = "가게 목록 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<MyStoreResopnse>> getMyStoreList(
		@Parameter(hidden = true) UserPassport userPassport
	);

}
