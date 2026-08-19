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
class WithdrawUserServiceTest {

	@Mock private UserPersistencePort userPersistencePort;
	@Mock private AuthTokenService authTokenService;

	private WithdrawUserService withdrawUserService;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@BeforeEach
	void setUp() {
		withdrawUserService = new WithdrawUserService(
				userPersistencePort,
				authTokenService,
				new UserDomainExceptionMapper()
		);
	}

	@Test
	@DisplayName("회원탈퇴 성공 시 DELETED 저장 및 Refresh 파기")
	void withdraw_success() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

		withdrawUserService.withdraw(USER_UUID.toString());

		then(userPersistencePort).should().save(any(User.class));
		then(authTokenService).should().revokeTokens(USER_UUID);
	}

	@Test
	@DisplayName("존재하지 않는 사용자 탈퇴 실패")
	void withdraw_user_not_found() {
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> withdrawUserService.withdraw(USER_UUID.toString()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_NOT_FOUND);
	}
}
