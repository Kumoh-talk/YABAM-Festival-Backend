package com.application.presentation.menu.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.menu.dto.request.PatchMenuCategoryInfoRequest;
import com.application.presentation.menu.dto.request.PostMenuCategoryInfoRequest;
import com.application.presentation.menu.dto.response.MenuCategoryInfoResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public interface MenuCategoryApi {
	@Operation(
		summary = "메뉴 카테고리 생성 API",
		description = "메뉴 카테고리를 생성합니다." + " 단, 영업중인 경우에는 생성할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuCategoryInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuCategoryInfoResponse.class,
			description = "메뉴 카테고리 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.EXIST_MENU_CATEGORY_ORDER)
		}
	)
	ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> postMenuCategory(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId,
		@RequestBody @Valid @NotNull PostMenuCategoryInfoRequest postMenuCategoryInfoRequest);

	@Operation(
		summary = "메뉴 카테고리 리스트 조회 API",
		description = "메뉴 카테고리 리스트를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuCategoryInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuCategoryInfoResponse.class,
			description = "메뉴 카테고리 리스트 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE)
		}
	)
	ResponseEntity<ResponseBody<List<MenuCategoryInfoResponse>>> getMenuCategoryList(
		@PathVariable Long storeId);

	@Operation(
		summary = "메뉴 카테고리 이름 수정 API",
		description = "메뉴 카테고리 이름을 수정합니다." + " 단, 영업중인 경우에는 수정할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuCategoryInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuCategoryInfoResponse.class,
			description = "메뉴 카테고리 이름 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_CATEGORY_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> patchMenuCategoryInfo(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId,
		@RequestBody @Valid @NotNull PatchMenuCategoryInfoRequest patchMenuCategoryInfoRequest);

	@Operation(
		summary = "메뉴 카테고리 순서 수정 API",
		description = "메뉴 카테고리 순서를 수정합니다." + " 단, 영업중인 경우에는 수정할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuCategoryInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuCategoryInfoResponse.class,
			description = "메뉴 카테고리 순서 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_CATEGORY_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> patchMenuCategoryOrder(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId,
		@RequestParam @Min(1) @NotNull Integer patchOrder);

	@Operation(
		summary = "메뉴 카테고리 삭제 API",
		description = "메뉴 카테고리를 삭제합니다." + " 단, 영업중인 경우에는 삭제할 수 없습니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "메뉴 카테고리 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_CATEGORY_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteMenuCategory(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId);
}
