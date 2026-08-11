package com.plip.user.global.exception;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("BusinessException: {} - {}", errorCode.getCode(), e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.of(errorCode.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.orElse(ErrorCode.INVALID_INPUT.getMessage());
		log.warn("Validation failed: {}", message);
		return ResponseEntity
				.status(ErrorCode.INVALID_INPUT.getHttpStatus())
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT.getCode(), message));
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception", e);
		ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.of(errorCode.getCode(), errorCode.getMessage()));
	}
}
