package com.plip.user.adapter.in.web.dto;

import com.plip.user.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원탈퇴 응답")
public class UserWithdrawResponse {

	@Schema(description = "결과 코드", example = "USER_S001")
	private String code;

	@Schema(description = "결과 메시지", example = "회원탈퇴가 완료되었습니다.")
	private String message;

	private UserWithdrawResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public static UserWithdrawResponse of() {
		return new UserWithdrawResponse(
				SuccessCode.WITHDRAW_COMPLETED.getCode(),
				SuccessCode.WITHDRAW_COMPLETED.getMessage()
		);
	}
}
