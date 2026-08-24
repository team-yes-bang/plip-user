package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 복구 pending 요청")
public class SocialRestorePendingRequest {

	@NotBlank(message = "pending 토큰은 필수입니다.")
	@Schema(description = "소셜 OAuth pending 토큰")
	private String pendingToken;
}
