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
@Schema(description = "비밀번호 재설정 요청")
public class PasswordResetRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Schema(description = "계정 이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "인증 토큰은 필수입니다.")
	@Schema(description = "이메일 OTP 인증 완료 토큰")
	private String verificationToken;

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
	@Schema(description = "새 비밀번호", example = "newPassword123!")
	private String newPassword;
}
