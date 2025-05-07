package com.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_0001", "잘못된 입력 값입니다."),
	// Owner
	NOT_VALID_OWNER(HttpStatus.BAD_REQUEST, "OWNER_0001", "해당 사용자는 가게 점주가 아닙니다"),

	// User
	USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_0001", "해당 사용자는 요청한 리소스에 접근할 수 없습니다."),

	// Store
	NOT_EQUAL_STORE_OWNER(HttpStatus.CONFLICT, "STORE_0001", "해당 가게의 점주가 아닙니다"),
	NOT_FOUND_STORE(HttpStatus.NOT_FOUND, "STORE_0002", "해당 가게를 찾을 수 없습니다"),
	CONFLICT_OPEN_STORE(HttpStatus.CONFLICT, "STORE_0003", "해당 가게 활성화 여부가 충돌된 요청입니다"),
	CONFLICT_CLOSE_STORE(HttpStatus.CONFLICT, "STORE_004", "가게가 이미 종료되었습니다."),

	// Auth
	INVALID_ID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_0001", "해당 ID 토큰은 유효하지 않습니다."),
	ABNORMAL_ID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_0002", "해당 ID 토큰은 정상적이지 않습니다"),
	NOT_MATCHED_PUBLIC_KEY(HttpStatus.NOT_FOUND, "AUTH_0003", "해당 ID 토큰의 공개키를 찾을 수 없습니다"),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_0004", "비밀번호가 일치하지 않습니다."),
	USERID_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_0005", "존재하지 않는 아이디입니다."),
	EXIST_SAME_USERID(HttpStatus.CONFLICT, "AUTH_0006", "이미 사용중인 아이디 입니다."),
	EXIST_SAME_NICKNAME(HttpStatus.CONFLICT, "AUTH_0007", "이미 사용중인 닉네임 입니다."),
	ADDITIONAL_INFO_NOT_UPDATED(HttpStatus.UNAUTHORIZED, "AUTH_0008", "추가정보가 업데이트되지 않았습니다."),

	// Menu
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_0001", "존재하지 않는 메뉴입니다."),
	EXIST_MENU_ORDER(HttpStatus.CONFLICT, "MENU_0002", "이미 존재하는 메뉴 순서입니다."),
	STORE_IS_OPEN_MENU_WRITE(HttpStatus.CONFLICT, "MENU_0003", "운영중인 가게는 메뉴를 추가 및 수정할 수 없습니다."),
	MENU_ORDER_INVALID(HttpStatus.BAD_REQUEST, "MENU_0004", "메뉴 순서가 유효하지 않습니다."),
	MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "MENU_0005", "메뉴가 품절되었습니다."),

	// MenuCategory
	MENU_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_CATEGORY_0001", "존재하지 않는 메뉴 카테고리입니다."),
	EXIST_MENU_CATEGORY_ORDER(HttpStatus.CONFLICT, "MENU_CATEGORY_0002", "이미 존재하는 메뉴 카테고리 순서입니다."),
	MENU_CATEGORY_ORDER_INVALID(HttpStatus.BAD_REQUEST, "MENU_CATEGORY_0003", "메뉴 카테고리 순서가 유효하지 않습니다."),
	// Security
	NEED_AUTHORIZED(HttpStatus.UNAUTHORIZED, "SECURITY_0001", "인증이 필요합니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "SECURITY_0002", "권한이 없습니다."),
	JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "SECURITY_0003", "JWT 토큰이 만료되었습니다."),
	JWT_INVALID(HttpStatus.UNAUTHORIZED, "SECURITY_0004", "JWT 토큰이 올바르지 않습니다."),
	JWT_NOT_EXIST(HttpStatus.UNAUTHORIZED, "SECURITY_0005", "JWT 토큰이 존재하지 않습니다."),

	// Table
	EXIST_TABLE(HttpStatus.CONFLICT, "TABLE_0001", "이미 존재하는 테이블입니다"),
	NOT_FOUND_TABLE(HttpStatus.NOT_FOUND, "TABLE_0002", "존재하지 않는 테이블입니다."),
	ALREADY_ACTIVE_TABLE(HttpStatus.CONFLICT, "TABLE_0003", "이미 활성화된 테이블입니다."),
	STORE_IS_OPEN_TABLE_WRITE(HttpStatus.CONFLICT, "TABLE_0004", "가게가 운영중입니다. 테이블 수 조정 불가"),
	TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "TABLE_0005", "테이블이 존재하지 않습니다."),
	TABLE_NOT_EQUAL_MODIFY(HttpStatus.CONFLICT, "TABLE_0006", "테이블 수가 같습니다."),
	// Sale
	NOT_FOUND_SALE(HttpStatus.NOT_FOUND, "SALE_0001", "존재하지 않는 세일입니다."),
	CLOSE_SALE(HttpStatus.CONFLICT, "SALE_0002", "영업이 종료되었습니다."),
	SALE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SALE_0003", "대상 영업에 접근 가능한 요청이 아닙니다."),

	// Receipt
	RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECEIPT_0001", "존재하지 않는 영수증입니다."),
	ALREADY_ADJUSTMENT_RECEIPT(HttpStatus.CONFLICT, "RECEIPT_0002", "이미 정산된 영수증입니다."),
	RECEIPT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RECEIPT_0003", "대상 영수증에 접근 가능한 요청이 아닙니다."),

	// Order
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_0001", "존재하지 않는 주문입니다."),
	ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_0002", "대상 주문에 접근 가능한 요청이 아닙니다."),
	INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "ORDER_0003", "주문 상태 전환이 불가능합니다."),
	ALREADY_RECEIVED_ORDER(HttpStatus.BAD_REQUEST, "ORDER_0004", "이미 접수된 주문입니다."),
	ALREADY_CANCELED_ORDER(HttpStatus.BAD_REQUEST, "ORDER_0005", "이미 취소된 주문입니다."),
	ALREADY_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "ORDER_0006", "이미 완료된 주문입니다."),
	TRANSFER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "ORDER_0007", "주문 상태 전환이 불가능합니다."),
	ORDER_STATUS_NOT_RECEIVED(HttpStatus.BAD_REQUEST, "ORDER_0008", "주문 상태가 접수되지 않았습니다."),

	// VoException
	NOT_VALID_VO(HttpStatus.BAD_REQUEST, "VO_0001", "Vo 객체가 유효하지 않습니다."),

	// OrderMenu
	ORDER_MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_MENU_0001", "존재하지 않는 주문 메뉴입니다."),
	ORDER_MENU_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_MENU_0002", "대상 주문 메뉴에 접근 가능한 요청이 아닙니다."),
	ALREADY_COOKING_ORDER_MENU(HttpStatus.BAD_REQUEST, "ORDER_MENU_0003", "이미 조리중인 메뉴입니다."),
	ALREADY_CANCELED_ORDER_MENU(HttpStatus.BAD_REQUEST, "ORDER_MENU_0004", "이미 취소된 메뉴입니다."),
	ALREADY_COMPLETED_ORDER_MENU(HttpStatus.BAD_REQUEST, "ORDER_MENU_0005", "이미 완료된 메뉴입니다."),

	// Cart
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_0001", "존재하지 않는 장바구니입니다."),
	CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_0002", "장바구니가 비어있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
