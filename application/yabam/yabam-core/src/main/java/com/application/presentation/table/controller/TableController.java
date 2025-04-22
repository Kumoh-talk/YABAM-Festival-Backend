package com.application.presentation.table.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.table.dto.TableInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import domain.pos.table.service.TableService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TableController {
	private final TableService tableService;

	@PostMapping("/api/v1/table")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<TableInfoResponse>> createTable(
		final UserPassport userPassport,
		@RequestParam Long storeId,
		@RequestParam Integer tableNumber) {
		return ResponseEntity.ok(
			createSuccessResponse(TableInfoResponse.from(tableService.createTable(userPassport, storeId, tableNumber)))
		);
	}

	@PatchMapping("/api/v1/table")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> updateTableNum(
		final UserPassport userPassport,
		@RequestParam Long tableId,
		@RequestParam Integer tableNumber) {
		tableService.updateTableNum(userPassport, tableId, tableNumber);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/table")
	@HasRole(userRole = ROLE_ANONYMOUS)
	public ResponseEntity<ResponseBody<TableInfoResponse>> getTables(
		@RequestParam Long storeId) {
		return ResponseEntity.ok(
			createSuccessResponse(TableInfoResponse.from(tableService.findTables(storeId)))
		);
	}
}
