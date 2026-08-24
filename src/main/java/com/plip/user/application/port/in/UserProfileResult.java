package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResult {

	private final String userUuid;
	private final String nickname;
	private final String profileImagePath;
	private final String email;
	private final boolean hasLocalAuth;

	public static UserProfileResult of(
			String userUuid,
			String nickname,
			String profileImagePath,
			String email,
			boolean hasLocalAuth
	) {
		return new UserProfileResult(userUuid, nickname, profileImagePath, email, hasLocalAuth);
	}
}
