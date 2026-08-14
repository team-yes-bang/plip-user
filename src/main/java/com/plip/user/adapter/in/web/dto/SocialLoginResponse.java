package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 로그인 응답")
public class SocialLoginResponse extends AuthTokenResponse {

	@Schema(description = "사용자 UUID", example = "01912345-6789-7abc-def0-123456789abc")
	private String userUuid;

	@Schema(description = "신규 가입 여부", example = "false")
	private boolean newUser;

	private SocialLoginResponse(
			String userUuid,
			boolean newUser,
			String accessToken,
			String refreshToken,
			long accessTokenExpiresIn
	) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
		this.userUuid = userUuid;
		this.newUser = newUser;
	}

	public static SocialLoginResponse of(String userUuid, boolean newUser, AuthTokenResult tokens) {
		return new SocialLoginResponse(
				userUuid,
				newUser,
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
