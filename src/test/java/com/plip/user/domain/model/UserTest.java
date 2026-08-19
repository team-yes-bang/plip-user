package com.plip.user.domain.model;

import com.plip.user.domain.exception.UserDomainError;
import com.plip.user.domain.exception.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("ACTIVE 계정 탈퇴 성공")
	void withdraw_success() {
		User user = activeUser();

		user.withdraw(LocalDateTime.now());

		assertThat(user.getStatus()).isEqualTo(User.STATUS_DELETED);
		assertThat(user.getDeletedAt()).isNotNull();
	}

	@Test
	@DisplayName("이미 탈퇴한 계정은 탈퇴 불가")
	void withdraw_already_deleted() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(),
				LocalDateTime.now().minusDays(1));

		assertThatThrownBy(() -> user.withdraw(LocalDateTime.now()))
				.isInstanceOf(UserDomainException.class)
				.extracting(e -> ((UserDomainException) e).getError())
				.isEqualTo(UserDomainError.ALREADY_WITHDRAWN);
	}

	@Test
	@DisplayName("유예 기간 내 복구 성공")
	void restore_success() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(),
				LocalDateTime.now().minusDays(10));

		user.restore(LocalDateTime.now());

		assertThat(user.getStatus()).isEqualTo(User.STATUS_ACTIVE);
		assertThat(user.getDeletedAt()).isNull();
	}

	@Test
	@DisplayName("유예 기간 만료 복구 불가")
	void restore_grace_expired() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(),
				LocalDateTime.now().minusDays(31));

		assertThatThrownBy(() -> user.restore(LocalDateTime.now()))
				.isInstanceOf(UserDomainException.class)
				.extracting(e -> ((UserDomainException) e).getError())
				.isEqualTo(UserDomainError.GRACE_EXPIRED);
	}

	private User activeUser() {
		return User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);
	}
}
