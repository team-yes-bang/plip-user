package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "토큰 재발급 요청")
public class TokenReissueRequest {

	@NotBlank(message = "리프레시 토큰은 필수입니다.")
	@Schema(description = "리프레시 토큰")
	private String refreshToken;
}
