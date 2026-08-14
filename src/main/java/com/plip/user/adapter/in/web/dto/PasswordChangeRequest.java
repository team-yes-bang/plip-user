package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "비밀번호 변경 요청")
public class PasswordChangeRequest {

	@NotBlank(message = "현재 비밀번호는 필수입니다.")
	@Schema(description = "현재 비밀번호", example = "password123!")
	private String currentPassword;

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
	@Schema(description = "새 비밀번호", example = "newPassword123!")
	private String newPassword;
}
