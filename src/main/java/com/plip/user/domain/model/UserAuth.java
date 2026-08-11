package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuth {

	private Long id;
	private Long userId;
	private String authType;
	private String email;
	private String passwordHash;
	private String provider;
	private String providerUserId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public static UserAuth of(
			Long id,
			Long userId,
			String authType,
			String email,
			String passwordHash,
			String provider,
			String providerUserId,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		UserAuth userAuth = new UserAuth();
		userAuth.id = id;
		userAuth.userId = userId;
		userAuth.authType = authType;
		userAuth.email = email;
		userAuth.passwordHash = passwordHash;
		userAuth.provider = provider;
		userAuth.providerUserId = providerUserId;
		userAuth.createdAt = createdAt;
		userAuth.updatedAt = updatedAt;
		userAuth.deletedAt = deletedAt;
		return userAuth;
	}
}
