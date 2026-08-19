package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "계정 복구 응답")
public class UserRestoreResponse {

	@Schema(description = "결과 메시지", example = "계정이 복구되었습니다.")
	private String message;

	private UserRestoreResponse(String message) {
		this.message = message;
	}

	public static UserRestoreResponse of() {
		return new UserRestoreResponse("계정이 복구되었습니다.");
	}
}
