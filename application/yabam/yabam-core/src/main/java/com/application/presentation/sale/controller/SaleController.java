package com.application.presentation.sale.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.sale.dto.response.SaleIdResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.service.SaleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SaleController {
	private final SaleService saleService;

	@PostMapping("/api/v1/sale/open")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<SaleIdResponse>> openStore(
		UserPassport userPassport,
		@RequestParam Long storeId
	) {
		return ResponseEntity.ok(createSuccessResponse(
				SaleIdResponse.from(saleService.openStore(userPassport, storeId))
			)
		);
	}

	@PatchMapping("/api/v1/sale/close")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> closeStore(
		UserPassport userPassport,
		@RequestParam Long storeId
	) {
		saleService.closeStore(userPassport, storeId);
		return ResponseEntity.ok(createSuccessResponse());
	}

}
