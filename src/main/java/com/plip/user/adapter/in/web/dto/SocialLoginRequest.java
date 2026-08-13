package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 로그인 요청")
public class SocialLoginRequest {

	@NotBlank(message = "액세스 토큰은 필수입니다.")
	@Schema(description = "소셜 제공자의 액세스 토큰", example = "ya29.a0AfH6SM...")
	private String accessToken;

	@Valid
	@Schema(description = "약관 동의 목록 (신규 가입 시 필수)")
	private List<TermAgreementRequest> termsAgreements;
}
