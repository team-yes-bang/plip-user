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
@Schema(description = "소셜 가입 완료 요청")
public class SocialSignupCompleteRequest {

	@NotBlank(message = "pending 토큰은 필수입니다.")
	@Schema(description = "소셜 가입 pending 토큰")
	private String pendingToken;

	@Valid
	@Schema(description = "약관 동의 목록")
	private List<TermAgreementRequest> termsAgreements;
}
