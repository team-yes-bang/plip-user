package com.plip.user.domain.model;

import com.plip.user.domain.exception.UserDomainError;
import com.plip.user.domain.exception.UserDomainException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETED = "DELETED";
	public static final int WITHDRAWAL_GRACE_DAYS = 30;

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

	public void withdraw(LocalDateTime now) {
		if (isDeleted()) {
			throw new UserDomainException(UserDomainError.ALREADY_WITHDRAWN);
		}
		if (!STATUS_ACTIVE.equals(status)) {
			throw new UserDomainException(UserDomainError.NOT_ACTIVE);
		}
		this.status = STATUS_DELETED;
		this.deletedAt = now;
	}

	public void restore(LocalDateTime now) {
		if (!isDeleted()) {
			throw new UserDomainException(UserDomainError.NOT_DELETED);
		}
		if (!isWithinWithdrawalGracePeriod(now)) {
			throw new UserDomainException(UserDomainError.GRACE_EXPIRED);
		}
		this.status = STATUS_ACTIVE;
		this.deletedAt = null;
	}

	public boolean isDeleted() {
		return STATUS_DELETED.equals(status) || deletedAt != null;
	}

	public boolean isWithinWithdrawalGracePeriod(LocalDateTime now) {
		if (deletedAt == null) {
			return false;
		}
		return !deletedAt.plusDays(WITHDRAWAL_GRACE_DAYS).isBefore(now);
	}
}
