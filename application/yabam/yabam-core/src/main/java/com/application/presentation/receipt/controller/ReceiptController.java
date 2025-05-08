package com.application.presentation.receipt.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.global.response.GlobalPageResponse;
import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.receipt.api.ReceiptApi;
import com.application.presentation.receipt.dto.response.ReceiptAndOrdersResponse;
import com.application.presentation.receipt.dto.response.ReceiptInfoResponse;
import com.application.presentation.receipt.dto.response.ReceiptResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.service.ReceiptService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class ReceiptController implements ReceiptApi {
	private final ReceiptService receiptService;

	@PostMapping("/api/v1/receipts")
	public ResponseEntity<ResponseBody<ReceiptResponse>> registerReceipt(
		@RequestParam @NotNull Long queryTableId, @RequestParam @NotNull Long querySaleId) {
		Receipt receipt = receiptService.registerReceipt(queryTableId, querySaleId);
		return ResponseEntity.ok(createSuccessResponse(ReceiptResponse.from(receipt)));
	}

	@GetMapping("/api/v1/receipts/{receiptId}")
	public ResponseEntity<ResponseBody<ReceiptInfoResponse>> getReceiptInfo(@PathVariable Long receiptId) {
		ReceiptInfo receiptInfo = receiptService.getReceiptInfo(receiptId);
		return ResponseEntity.ok(createSuccessResponse(ReceiptInfoResponse.from(receiptInfo)));
	}

	@GetMapping("/api/v1/sales/{saleId}/receipts")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<GlobalPageResponse<ReceiptInfoResponse>>> getAdjustedReceiptPageBySale(
		UserPassport userPassport,
		@PathVariable Long saleId,
		@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<ReceiptInfoResponse> receiptInfoPageResponse = receiptService.getAdjustedReceiptPageBySale(pageable,
			userPassport, saleId).map(ReceiptInfoResponse::from);
		return ResponseEntity.ok(
			createSuccessResponse(GlobalPageResponse.create(receiptInfoPageResponse)));
	}

	@PatchMapping("/api/v1/receipts/stop")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<List<ReceiptAndOrdersResponse>>> stopReceiptUsage(
		UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds) {
		List<ReceiptAndOrdersResponse> receipts = receiptService.stopReceiptUsage(receiptIds, userPassport)
			.stream().map(ReceiptAndOrdersResponse::from)
			.toList();
		return ResponseEntity.ok(createSuccessResponse(receipts));
	}

	@PatchMapping("/api/v1/receipts/re-start")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> restartReceiptUsage(
		UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds) {
		receiptService.restartReceiptUsage(receiptIds, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@PatchMapping("/api/v1/receipts/adjust")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> adjustReceipts(
		UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds) {
		receiptService.adjustReceipts(receiptIds, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@DeleteMapping("/api/v1/receipts/{receiptId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteReceipt(
		UserPassport userPassport,
		@PathVariable Long receiptId) {
		receiptService.deleteReceipt(receiptId, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/table/{tableId}/receipts/non-adjust")
	public ResponseEntity<ResponseBody<Long>> getNonAdjustReceiptId(@PathVariable Long tableId) {
		Long receiptId = receiptService.getNonAdjustReceiptId(tableId);
		return ResponseEntity.ok(createSuccessResponse(receiptId));
	}

	@GetMapping("/api/v1/customers/{customerId}/receipts")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<GlobalSliceResponse<ReceiptInfoResponse>>> getCustomerReceiptSlice(
		UserPassport userPassport,
		@PathVariable Long customerId,
		@RequestParam @Min(1) int pageSize, @RequestParam(required = false) Long lastReceiptId) {
		Slice<ReceiptInfoResponse> receipts = receiptService.getCustomerReceiptSlice(pageSize, userPassport, customerId,
			lastReceiptId).map(receipt -> ReceiptInfoResponse.from(receipt.getReceiptInfo()));
		return ResponseEntity.ok(createSuccessResponse(GlobalSliceResponse.from(receipts)));
	}
}
