package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로필 수정 요청")
public class ProfileUpdateRequest {

	@Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
	@Schema(description = "닉네임 (선택). 미전달 시 유지", example = "플립이")
	private String nickname;

	@Size(max = 255, message = "프로필 이미지 경로는 255자 이하여야 합니다.")
	@Schema(description = "프로필 이미지 상대 경로 (선택). 미전달 시 유지, 빈 문자열이면 제거", example = "profiles/user.png")
	private String profileImagePath;
}
