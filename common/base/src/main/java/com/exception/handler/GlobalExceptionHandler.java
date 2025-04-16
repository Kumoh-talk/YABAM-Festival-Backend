package com.exception.handler;

import static com.exception.ErrorCode.*;
import static com.response.ResponseUtil.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.exception.VoException;
import com.response.ResponseBody;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class) // custom 에러
	public ResponseEntity<ResponseBody<Void>> handleServiceException(HttpServletRequest request,
		ServiceException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
			.body(createFailureResponse(errorCode));
	}

	@ExceptionHandler(VoException.class)
	public ResponseEntity<ResponseBody<Void>> handleVoException(HttpServletRequest request,
		VoException exception) {
		String message = exception.getMessage();
		return ResponseEntity.status(NOT_VALID_VO.getStatus())
			.body(createFailureResponse(NOT_VALID_VO, message));
	}
}
