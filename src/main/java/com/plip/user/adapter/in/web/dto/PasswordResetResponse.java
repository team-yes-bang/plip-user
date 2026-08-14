package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "비밀번호 재설정 응답")
public class PasswordResetResponse {

	@Schema(description = "결과 메시지", example = "비밀번호가 재설정되었습니다.")
	private String message;

	private PasswordResetResponse(String message) {
		this.message = message;
	}

	public static PasswordResetResponse of() {
		return new PasswordResetResponse("비밀번호가 재설정되었습니다.");
	}
}
