package com.application.presentation.table.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.table.api.TableApi;
import com.application.presentation.table.dto.request.TableCreateRequest;
import com.application.presentation.table.dto.request.TableModifyRequest;
import com.application.presentation.table.dto.response.TableIdResponse;
import com.application.presentation.table.dto.response.TableInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.table.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TableController implements TableApi {
	private final TableService tableService;

	@PostMapping("/api/v1/table")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<TableIdResponse>> createTable(
		final UserPassport userPassport,
		@RequestBody @Valid TableCreateRequest tableCreateRequest) {
		return ResponseEntity.ok(
			createSuccessResponse(TableIdResponse.from(
				tableService.createTable(userPassport,
					tableCreateRequest.storeId(),
					tableCreateRequest.tableNumber(),
					tableCreateRequest.getTablePoint())))
		);
	}

	@PatchMapping("/api/v1/table")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> updateTable(
		final UserPassport userPassport,
		@RequestBody @Valid TableModifyRequest tableModifyRequest) {
		tableService.updateTable(
			userPassport,
			tableModifyRequest.tableId(),
			tableModifyRequest.tableNumber(),
			tableModifyRequest.getTablePoint());
		return ResponseEntity
			.ok(createSuccessResponse());
	}

	@DeleteMapping("/api/v1/table")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteTable(
		final UserPassport userPassport,
		@RequestParam UUID tableId) {
		tableService.deleteTable(userPassport, tableId);
		return ResponseEntity
			.ok(createSuccessResponse());
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
