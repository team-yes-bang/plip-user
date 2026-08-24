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

	// Auth / JWT
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다."),
	ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_002", "액세스 토큰이 유효하지 않습니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_003", "리프레시 토큰이 유효하지 않습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_004", "사용자를 찾을 수 없습니다."),
	USER_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_005", "비활성화된 계정입니다."),
	USER_DELETED_RESTORABLE(HttpStatus.FORBIDDEN, "AUTH_010", "탈퇴 유예 기간 중인 계정입니다. 계정 복구가 필요합니다."),
	USER_WITHDRAWAL_GRACE_EXPIRED(HttpStatus.FORBIDDEN, "AUTH_011", "탈퇴 유예 기간이 만료된 계정입니다."),
	USER_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_008", "계정 이용이 제한되었습니다."),
	PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "AUTH_006", "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
	LOCAL_ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_007", "로컬 계정이 존재하지 않습니다."),
	CURRENT_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_009", "현재 비밀번호가 올바르지 않습니다."),

	// Profile
	PROFILE_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "PROFILE_002", "수정할 항목이 없습니다."),

	// Terms
	REQUIRED_TERM_REVOKE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "TERMS_001", "필수 약관은 철회할 수 없습니다."),
	TERMS_AGREEMENTS_EMPTY(HttpStatus.BAD_REQUEST, "TERMS_002", "변경할 약관 동의 항목이 없습니다."),

	// Notification
	NOTIFICATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFY_001", "알림 설정을 찾을 수 없습니다."),

	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
