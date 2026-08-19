package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 계정 복구 요청")
public class SocialRestoreRequest {

	@NotBlank(message = "액세스 토큰은 필수입니다.")
	@Schema(description = "소셜 제공자의 액세스 토큰", example = "ya29.a0AfH6SM...")
	private String accessToken;
}
