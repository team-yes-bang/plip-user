package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 OTP 검증 요청")
public class EmailOtpVerifyRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Schema(description = "인증 대상 이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "인증번호는 필수입니다.")
	@Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
	@Schema(description = "6자리 인증번호", example = "123456")
	private String otpCode;
}
