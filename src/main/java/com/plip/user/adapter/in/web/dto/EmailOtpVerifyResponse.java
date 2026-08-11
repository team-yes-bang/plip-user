package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 OTP 검증 응답")
public class EmailOtpVerifyResponse {

	@Schema(description = "이메일 인증 완료 토큰 (회원가입 시 사용)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
	private String verificationToken;

	private EmailOtpVerifyResponse(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public static EmailOtpVerifyResponse of(String verificationToken) {
		return new EmailOtpVerifyResponse(verificationToken);
	}
}
