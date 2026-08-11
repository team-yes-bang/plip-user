package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "에러 응답")
public class ErrorResponse {

	@Schema(description = "에러 코드", example = "OTP_001")
	private String code;

	@Schema(description = "에러 메시지", example = "요청 횟수가 제한을 초과했습니다.")
	private String message;

	private ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message);
	}
}
