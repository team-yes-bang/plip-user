package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "JWT 토큰 응답")
public class AuthTokenResponse {

	@Schema(description = "액세스 토큰")
	private String accessToken;

	@Schema(description = "리프레시 토큰")
	private String refreshToken;

	@Schema(description = "액세스 토큰 만료 시간(초)", example = "3600")
	private long accessTokenExpiresIn;

	protected AuthTokenResponse(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.accessTokenExpiresIn = accessTokenExpiresIn;
	}

	public static AuthTokenResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		return new AuthTokenResponse(accessToken, refreshToken, accessTokenExpiresIn);
	}
}
