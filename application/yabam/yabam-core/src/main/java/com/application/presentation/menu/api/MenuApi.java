package com.application.presentation.menu.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.menu.dto.request.PatchMenuInfoRequest;
import com.application.presentation.menu.dto.request.PostMenuInfoRequest;
import com.application.presentation.menu.dto.response.MenuInfoResponse;
import com.application.presentation.menu.dto.response.MenuResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메뉴 API")
public interface MenuApi {
	@Operation(
		summary = "메뉴 생성 API",
		description = "메뉴를 생성합니다." + " 단, 영업중인 경우에는 생성할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuResponse.class,
			description = "메뉴 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_CATEGORY_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.EXIST_MENU_ORDER),
		}
	)
	ResponseEntity<ResponseBody<MenuResponse>> postMenu(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId,
		@RequestBody @Valid @NotNull PostMenuInfoRequest postMenuInfoRequest);

	@Operation(
		summary = "메뉴 세부정보 조회 API",
		description = "메뉴 세부정보를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuInfoResponse.class,
			description = "메뉴 세부정보 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuInfoResponse>> getMenuInfo(
		@PathVariable Long storeId,
		@PathVariable Long menuId);

	@Operation(
		summary = "카테고리 별 메뉴 무한스크롤 페이지 조회 API",
		description = "카테고리 별 메뉴 무한스크롤 페이지를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = GlobalSliceResponse.class,
			description = "해당 카테고리 메뉴 무한 스크롤 페이지 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_CATEGORY_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<GlobalSliceResponse<MenuInfoResponse>>> getMenuSlice(
		@PathVariable Long storeId,
		@RequestParam @Min(1) int pageSize,
		@Schema(description = "이전 페이지 가장 마지막 menuId(첫 페이지 조회 시 생략)") @RequestParam(required = false) Long lastMenuId,
		@RequestParam @NotNull Long menuCategoryId);

	@Operation(
		summary = "메뉴 세부정보 수정 API",
		description = "메뉴 세부정보를 수정합니다." + " 순서정보는 해당 api로 수정할 수 없습니다."
			+ " 단, 영업중인 경우에는 수정할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuInfoResponse.class,
			description = "메뉴 세부정보 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuInfoResponse>> patchMenuInfo(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestBody @Valid @NotNull PatchMenuInfoRequest patchMenuInfoRequest);

	@Operation(
		summary = "메뉴 순서정보 수정 API",
		description = "메뉴 순서정보를 수정합니다."
			+ " 단, 영업중인 경우에는 수정할 수 없습니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuInfoResponse.class,
			description = "메뉴 순서정보 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuInfoResponse>> patchMenuOrder(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestParam @Min(1) @NotNull Integer patchOrder);

	@Operation(
		summary = "메뉴 매진정보 수정 API",
		description = "메뉴 매진정보를 수정합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = MenuInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = MenuInfoResponse.class,
			description = "메뉴 매진정보 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<MenuInfoResponse>> patchIsSoldOut(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestParam @NotNull Boolean isSoldOut);

	@Operation(
		summary = "메뉴 삭제 API",
		description = "메뉴를 삭제합니다." + " 단, 영업중인 경우에는 삭제할 수 없습니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "메뉴 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_MENU_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteMenu(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId);
}
