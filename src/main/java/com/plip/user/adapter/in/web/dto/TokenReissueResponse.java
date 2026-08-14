package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "토큰 재발급 응답")
public class TokenReissueResponse extends AuthTokenResponse {

	private TokenReissueResponse(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
	}

	public static TokenReissueResponse of(AuthTokenResult tokens) {
		return new TokenReissueResponse(
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
