package com.plip.user.adapter.in.web.dto;

import com.plip.user.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로그아웃 응답")
public class LogoutResponse {

	@Schema(description = "결과 코드", example = "AUTH_S001")
	private String code;

	@Schema(description = "결과 메시지", example = "로그아웃되었습니다.")
	private String message;

	private LogoutResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public static LogoutResponse of() {
		return new LogoutResponse(
				SuccessCode.LOGOUT_COMPLETED.getCode(),
				SuccessCode.LOGOUT_COMPLETED.getMessage()
		);
	}
}
