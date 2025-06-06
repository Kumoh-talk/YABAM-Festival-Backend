package com.response;

import com.exception.ErrorCode;

public class ResponseUtil {

	public static ResponseBody<Void> createSuccessResponse() {
		return new SuccessResponseBody<>();
	}

	public static <T> ResponseBody<T> createSuccessResponse(T data) {
		return new SuccessResponseBody<>(data);
	}

	public static ResponseBody<Void> createFailureResponse(ErrorCode errorCode) {
		return new FailedResponseBody(
			errorCode.getCode(),
			errorCode.getMessage()
		);
	}

	public static ResponseBody<Void> createFailureResponse(ErrorCode errorCode, String customMessage) {
		return new FailedResponseBody(
			errorCode.getCode(),
			customMessage
		);
	}
}
