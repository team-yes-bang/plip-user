package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	private Long id;
	private UuidV7 userUuid;
	private String nickname;
	private String profileImagePath;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public static User of(
			Long id,
			UuidV7 userUuid,
			String nickname,
			String profileImagePath,
			String status,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		User user = new User();
		user.id = id;
		user.userUuid = userUuid;
		user.nickname = nickname;
		user.profileImagePath = profileImagePath;
		user.status = status;
		user.createdAt = createdAt;
		user.updatedAt = updatedAt;
		user.deletedAt = deletedAt;
		return user;
	}
}
