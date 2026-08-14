package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로컬 로그인 응답")
public class LocalLoginResponse extends AuthTokenResponse {

	@Schema(description = "사용자 UUID", example = "01912345-6789-7abc-def0-123456789abc")
	private String userUuid;

	private LocalLoginResponse(String userUuid, String accessToken, String refreshToken, long accessTokenExpiresIn) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
		this.userUuid = userUuid;
	}

	public static LocalLoginResponse of(String userUuid, AuthTokenResult tokens) {
		return new LocalLoginResponse(
				userUuid,
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
