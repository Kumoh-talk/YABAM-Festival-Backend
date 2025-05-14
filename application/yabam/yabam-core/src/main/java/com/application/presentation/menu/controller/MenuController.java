package com.application.presentation.menu.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.menu.api.MenuApi;
import com.application.presentation.menu.dto.request.PatchMenuInfoRequest;
import com.application.presentation.menu.dto.request.PostMenuInfoRequest;
import com.application.presentation.menu.dto.response.MenuInfoResponse;
import com.application.presentation.menu.dto.response.MenuResponse;
import com.application.presentation.menu.dto.response.MenuSliceResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.service.MenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class MenuController implements MenuApi {
	private final MenuService menuService;

	// TODO : 메뉴 카테고리당 메뉴 생성 100개 이하로 제한되어있음 (사유 : order 재정렬 문제)
	@PostMapping("/api/v1/stores/{storeId}/menus")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuResponse>> postMenu(
		UserPassport userPassport,
		@PathVariable Long storeId,
		@RequestBody @Valid @NotNull PostMenuInfoRequest postMenuInfoRequest) {
		Menu menu = menuService.postMenu(storeId, userPassport, postMenuInfoRequest.menuCategoryId(),
			postMenuInfoRequest.toMenuInfo());
		return ResponseEntity.ok(createSuccessResponse(MenuResponse.from(menu)));
	}

	@GetMapping("/api/v1/stores/{storeId}/menus/{menuId}")
	public ResponseEntity<ResponseBody<MenuInfoResponse>> getMenuInfo(
		@PathVariable Long storeId,
		@PathVariable Long menuId) {
		MenuInfo menuInfo = menuService.getMenuInfo(storeId, menuId);
		return ResponseEntity.ok(createSuccessResponse(MenuInfoResponse.from(menuInfo)));
	}

	@GetMapping("/api/v1/stores/{storeId}/menus")
	public ResponseEntity<ResponseBody<GlobalSliceResponse<MenuSliceResponse>>> getMenuSlice(
		@PathVariable Long storeId,
		@RequestParam @Min(1) int pageSize,
		@RequestParam(required = false) Long lastMenuId,
		@RequestParam(required = false) Long lastMenuCategoryId) {
		Slice<MenuSliceResponse> menuSliceResponse = menuService.getMenuSlice(pageSize, lastMenuId, storeId,
			lastMenuCategoryId).map(MenuSliceResponse::from);
		return ResponseEntity.ok(createSuccessResponse(GlobalSliceResponse.from(menuSliceResponse)));
	}

	@GetMapping("/api/v1/stores/{storeId}/menu-category/{menuCategoryId}/menus")
	public ResponseEntity<ResponseBody<List<MenuInfoResponse>>> getCategoryMenuList(
		@PathVariable Long storeId, @PathVariable Long menuCategoryId) {
		List<MenuInfoResponse> menuSliceResponse = menuService.getCategoryMenuList(storeId, menuCategoryId).stream()
			.map(MenuInfoResponse::from)
			.toList();
		return ResponseEntity.ok(createSuccessResponse(menuSliceResponse));
	}

	@PatchMapping("/api/v1/stores/{storeId}/menus/{menuId}/info")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuInfoResponse>> patchMenuInfo(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestBody @Valid @NotNull PatchMenuInfoRequest patchMenuInfoRequest) {
		MenuInfo menuInfo = menuService.patchMenuInfo(storeId, userPassport, patchMenuInfoRequest.toMenuInfo(menuId));
		return ResponseEntity.ok(createSuccessResponse(MenuInfoResponse.from(menuInfo)));
	}

	@PatchMapping("/api/v1/stores/{storeId}/menus/{menuId}/order")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuInfoResponse>> patchMenuOrder(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestParam @Min(1) @NotNull Integer patchOrder) {
		MenuInfo menuInfo = menuService.patchMenuOrder(storeId, userPassport, menuId, patchOrder);
		return ResponseEntity.ok(createSuccessResponse(MenuInfoResponse.from(menuInfo)));
	}

	@PatchMapping("/api/v1/stores/{storeId}/menus/{menuId}/sold-out")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuInfoResponse>> patchIsSoldOut(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId,
		@RequestParam @NotNull Boolean isSoldOut) {
		MenuInfo menuInfo = menuService.patchIsSoldOut(storeId, userPassport, menuId, isSoldOut);
		return ResponseEntity.ok(createSuccessResponse(MenuInfoResponse.from(menuInfo)));
	}

	@DeleteMapping("/api/v1/stores/{storeId}/menus/{menuId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteMenu(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuId) {
		menuService.deleteMenu(storeId, userPassport, menuId);
		return ResponseEntity.ok().body(createSuccessResponse());
	}
}
