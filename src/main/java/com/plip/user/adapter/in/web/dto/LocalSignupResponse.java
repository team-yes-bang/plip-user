package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 회원가입 응답")
public class LocalSignupResponse extends AuthTokenResponse {

	@Schema(description = "사용자 UUID", example = "01912345-6789-7abc-def0-123456789abc")
	private String userUuid;

	@Schema(description = "결과 메시지", example = "회원가입이 완료되었습니다.")
	private String message;

	private LocalSignupResponse(
			String userUuid,
			String message,
			String accessToken,
			String refreshToken,
			long accessTokenExpiresIn
	) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
		this.userUuid = userUuid;
		this.message = message;
	}

	public static LocalSignupResponse of(String userUuid, AuthTokenResult tokens) {
		return new LocalSignupResponse(
				userUuid,
				"회원가입이 완료되었습니다.",
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
