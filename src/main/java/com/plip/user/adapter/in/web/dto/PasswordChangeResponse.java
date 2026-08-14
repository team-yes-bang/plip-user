package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "비밀번호 변경 응답")
public class PasswordChangeResponse extends AuthTokenResponse {

	@Schema(description = "결과 메시지", example = "비밀번호가 변경되었습니다.")
	private String message;

	private PasswordChangeResponse(
			String message,
			String accessToken,
			String refreshToken,
			long accessTokenExpiresIn
	) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
		this.message = message;
	}

	public static PasswordChangeResponse of(AuthTokenResult tokens) {
		return new PasswordChangeResponse(
				"비밀번호가 변경되었습니다.",
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
