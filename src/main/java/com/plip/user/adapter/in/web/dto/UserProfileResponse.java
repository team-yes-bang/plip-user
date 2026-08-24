package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.UserProfileResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "본인 프로필 응답")
public class UserProfileResponse {

	@Schema(description = "사용자 UUID", example = "01912345-6789-7abc-def0-123456789abc")
	private String userUuid;

	@Schema(description = "닉네임", example = "플립이")
	private String nickname;

	@Schema(description = "프로필 이미지 상대 경로", example = "profiles/user.png")
	private String profileImagePath;

	@Schema(description = "계정 이메일", example = "user@example.com")
	private String email;

	@Schema(description = "로컬(이메일·비밀번호) 계정 연동 여부")
	private boolean hasLocalAuth;

	private UserProfileResponse(
			String userUuid,
			String nickname,
			String profileImagePath,
			String email,
			boolean hasLocalAuth
	) {
		this.userUuid = userUuid;
		this.nickname = nickname;
		this.profileImagePath = profileImagePath;
		this.email = email;
		this.hasLocalAuth = hasLocalAuth;
	}

	public static UserProfileResponse of(UserProfileResult result) {
		return new UserProfileResponse(
				result.getUserUuid(),
				result.getNickname(),
				result.getProfileImagePath(),
				result.getEmail(),
				result.isHasLocalAuth()
		);
	}
}
