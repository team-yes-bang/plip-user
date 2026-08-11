package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 OTP 발송 응답")
public class EmailOtpRequestResponse {

	@Schema(description = "결과 메시지", example = "인증번호가 발송되었습니다.")
	private String message;

	private EmailOtpRequestResponse(String message) {
		this.message = message;
	}

	public static EmailOtpRequestResponse success() {
		return new EmailOtpRequestResponse("인증번호가 발송되었습니다.");
	}
}
