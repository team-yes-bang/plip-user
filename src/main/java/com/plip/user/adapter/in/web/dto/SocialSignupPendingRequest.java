package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 가입 pending 저장 요청")
public class SocialSignupPendingRequest {

	@NotBlank(message = "소셜 제공자는 필수입니다.")
	@Schema(description = "소셜 제공자", example = "kakao")
	private String provider;

	@NotBlank(message = "액세스 토큰은 필수입니다.")
	@Schema(description = "소셜 제공자 액세스 토큰")
	private String accessToken;
}
