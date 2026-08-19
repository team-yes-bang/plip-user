package com.plip.user.application.service;

import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RestoreUserServiceTest {

	@Mock private UserPersistencePort userPersistencePort;

	private RestoreUserService restoreUserService;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@BeforeEach
	void setUp() {
		restoreUserService = new RestoreUserService(userPersistencePort, new UserDomainExceptionMapper());
	}

	@Test
	@DisplayName("유예 기간 내 DELETED 계정 복구 성공")
	void restore_success() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(10);
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(), deletedAt);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

		restoreUserService.restore(USER_UUID.toString());

		then(userPersistencePort).should().save(any(User.class));
	}

	@Test
	@DisplayName("유예 기간 만료 계정 복구 실패")
	void restore_grace_expired() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(), deletedAt);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> restoreUserService.restore(USER_UUID.toString()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_WITHDRAWAL_GRACE_EXPIRED);
	}

	@Test
	@DisplayName("ACTIVE 계정 복구 실패")
	void restore_not_deleted() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> restoreUserService.restore(USER_UUID.toString()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}
}
