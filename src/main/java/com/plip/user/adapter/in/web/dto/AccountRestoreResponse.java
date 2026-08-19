package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "계정 복구 응답")
public class AccountRestoreResponse extends AuthTokenResponse {

	@Schema(description = "결과 코드", example = "USER_S002")
	private String code;

	@Schema(description = "결과 메시지", example = "계정이 복구되었습니다.")
	private String message;

	@Schema(description = "사용자 UUID", example = "01912345-6789-7abc-def0-123456789abc")
	private String userUuid;

	private AccountRestoreResponse(
			String code,
			String message,
			String userUuid,
			String accessToken,
			String refreshToken,
			long accessTokenExpiresIn
	) {
		super(accessToken, refreshToken, accessTokenExpiresIn);
		this.code = code;
		this.message = message;
		this.userUuid = userUuid;
	}

	public static AccountRestoreResponse of(String userUuid, AuthTokenResult tokens) {
		return new AccountRestoreResponse(
				SuccessCode.RESTORE_COMPLETED.getCode(),
				SuccessCode.RESTORE_COMPLETED.getMessage(),
				userUuid,
				tokens.getAccessToken(),
				tokens.getRefreshToken(),
				tokens.getAccessTokenExpiresIn()
		);
	}
}
