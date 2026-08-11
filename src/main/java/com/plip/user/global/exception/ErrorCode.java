package com.plip.user.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// OTP
	OTP_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "OTP_001", "요청 횟수가 제한을 초과했습니다."),
	OTP_EXPIRED_OR_NOT_FOUND(HttpStatus.BAD_REQUEST, "OTP_002", "인증번호가 만료되었거나 존재하지 않습니다."),
	OTP_MISMATCH(HttpStatus.BAD_REQUEST, "OTP_003", "인증번호가 일치하지 않습니다."),

	// Email
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_001", "이메일 발송에 실패했습니다."),

	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
