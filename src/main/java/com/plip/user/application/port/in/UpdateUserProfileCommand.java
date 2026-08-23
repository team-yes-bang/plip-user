package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserProfileCommand {

	private final String userUuid;
	private final String nickname;
	private final String profileImagePath;

	public static UpdateUserProfileCommand of(String userUuid, String nickname, String profileImagePath) {
		return new UpdateUserProfileCommand(userUuid, nickname, profileImagePath);
	}

	public boolean hasNickname() {
		return nickname != null;
	}

	public boolean hasProfileImagePath() {
		return profileImagePath != null;
	}

	public boolean isEmpty() {
		return !hasNickname() && !hasProfileImagePath();
	}
}
