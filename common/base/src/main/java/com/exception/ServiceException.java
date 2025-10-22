package com.exception;

import java.util.Map;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
	private final ErrorCode errorCode;
	private final Map<String, Object> errorData;

	public ServiceException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.errorData = null;
	}

	public ServiceException(ErrorCode errorCode, Map<String, Object> errorData) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.errorData = errorData;
	}
}
