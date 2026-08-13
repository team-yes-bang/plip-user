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

	// Signup
	VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "SIGNUP_001", "이메일 인증 토큰이 유효하지 않습니다."),
	EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SIGNUP_002", "이미 가입된 이메일입니다."),
	REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "SIGNUP_003", "필수 약관에 모두 동의해야 합니다."),
	TERM_NOT_FOUND(HttpStatus.BAD_REQUEST, "SIGNUP_004", "존재하지 않는 약관입니다."),
	INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "SIGNUP_005", "닉네임은 2~12자여야 합니다."),

	// Social Login
	SOCIAL_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "SOCIAL_001", "지원하지 않는 소셜 로그인 제공자입니다."),
	SOCIAL_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "SOCIAL_002", "소셜 인증에 실패했습니다."),
	SOCIAL_SIGNUP_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "SOCIAL_003", "신규 소셜 사용자입니다. 약관 동의가 필요합니다."),

	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
