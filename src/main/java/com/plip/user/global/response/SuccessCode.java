package com.plip.user.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

	LOGOUT_COMPLETED("AUTH_S001", "로그아웃되었습니다."),
	WITHDRAW_COMPLETED("USER_S001", "회원탈퇴가 완료되었습니다."),
	RESTORE_COMPLETED("USER_S002", "계정이 복구되었습니다.");

	private final String code;
	private final String message;
}
