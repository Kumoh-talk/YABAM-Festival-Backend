package com.application.presentation.sale.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.sale.api.SaleApi;
import com.application.presentation.sale.dto.response.SaleCursorResponse;
import com.application.presentation.sale.dto.response.SaleIdResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.store.service.SaleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SaleController implements SaleApi {
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
		@RequestParam Long saleId
	) {
		saleService.closeStore(userPassport, saleId);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/sales")
	@HasRole(userRole = ROLE_OWNER)
	public ResponseEntity<ResponseBody<SaleCursorResponse>> getSales(
		@RequestParam Long storeId,
		@RequestParam(required = false) Long lastSaleId,
		@RequestParam Integer size
	) {
		return ResponseEntity.ok(createSuccessResponse(
				SaleCursorResponse.from(saleService.getSingleSalesByStore(storeId, lastSaleId, size))
			)
		);
	}

}
