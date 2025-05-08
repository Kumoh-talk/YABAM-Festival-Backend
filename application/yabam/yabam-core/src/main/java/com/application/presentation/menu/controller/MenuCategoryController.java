package com.application.presentation.menu.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import java.util.List;
import java.util.stream.Collectors;

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

import com.application.presentation.menu.api.MenuCategoryApi;
import com.application.presentation.menu.dto.request.PatchMenuCategoryInfoRequest;
import com.application.presentation.menu.dto.request.PostMenuCategoryInfoRequest;
import com.application.presentation.menu.dto.response.MenuCategoryInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.service.MenuCategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class MenuCategoryController implements MenuCategoryApi {
	private final MenuCategoryService menuCategoryService;

	@PostMapping("/api/v1/stores/{storeId}/menu-categories")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> postMenuCategory(
		UserPassport userPassport,
		@PathVariable Long storeId,
		@RequestBody @Valid @NotNull PostMenuCategoryInfoRequest postMenuCategoryInfoRequest) {
		MenuCategory menuCategory = menuCategoryService.postMenuCategory(
			storeId, userPassport, postMenuCategoryInfoRequest.toMenuCategoryInfo());
		return ResponseEntity.ok(
			createSuccessResponse(MenuCategoryInfoResponse.from(menuCategory.getMenuCategoryInfo())));
	}

	@GetMapping("/api/v1/stores/{storeId}/menu-categories")
	public ResponseEntity<ResponseBody<List<MenuCategoryInfoResponse>>> getMenuCategoryList(
		@PathVariable Long storeId) {
		return ResponseEntity.ok(createSuccessResponse(
			menuCategoryService.getMenuCategoryList(storeId).stream()
				.map(MenuCategoryInfoResponse::from).collect(Collectors.toList())));
	}

	@PatchMapping("/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}/info")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> patchMenuCategoryInfo(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId,
		@RequestBody @Valid @NotNull PatchMenuCategoryInfoRequest patchMenuCategoryInfoRequest) {
		MenuCategoryInfo patchMenuCategoryInfo = menuCategoryService.patchMenuCategory(
			storeId, userPassport, patchMenuCategoryInfoRequest.toMenuCategoryInfo(menuCategoryId));
		return ResponseEntity.ok(createSuccessResponse(MenuCategoryInfoResponse.from(patchMenuCategoryInfo)));
	}

	@PatchMapping("/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}/order")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<MenuCategoryInfoResponse>> patchMenuCategoryOrder(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId,
		@RequestParam @Min(1) @NotNull Integer patchOrder) {
		MenuCategoryInfo patchMenuCategoryInfo = menuCategoryService.patchMenuCategoryOrder(
			storeId, userPassport, menuCategoryId, patchOrder);
		return ResponseEntity.ok(createSuccessResponse(MenuCategoryInfoResponse.from(patchMenuCategoryInfo)));
	}

	@DeleteMapping("/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteMenuCategory(
		UserPassport userPassport,
		@PathVariable Long storeId, @PathVariable Long menuCategoryId) {
		menuCategoryService.deleteMenuCategory(storeId, userPassport, menuCategoryId);
		return ResponseEntity.ok().body(createSuccessResponse());
	}
}
