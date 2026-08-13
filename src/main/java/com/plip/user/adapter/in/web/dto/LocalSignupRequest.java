package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 회원가입 요청")
public class LocalSignupRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Schema(description = "가입 이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "인증 토큰은 필수입니다.")
	@Schema(description = "이메일 인증 완료 토큰", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
	private String verificationToken;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
	@Schema(description = "비밀번호", example = "password123!")
	private String password;

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
	@Schema(description = "닉네임", example = "플립이")
	private String nickname;

	@Valid
	@Schema(description = "약관 동의 목록 (필수 약관이 있을 경우 필수)")
	private List<TermAgreementRequest> termsAgreements;
}
