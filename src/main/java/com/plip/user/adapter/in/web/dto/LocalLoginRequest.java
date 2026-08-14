package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로컬 로그인 요청")
public class LocalLoginRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Schema(description = "로그인 이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
	@Schema(description = "비밀번호", example = "password123!")
	private String password;
}
