package com.plip.user.application.service;

import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserAccountStatusValidatorTest {

	@InjectMocks
	private UserAccountStatusValidator validator;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("ACTIVE 계정은 통과")
	void validateLoginEligible_active() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);
		validator.validateLoginEligible(user);
	}

	@Test
	@DisplayName("비활성(SUSPENDED 포함) 계정은 USER_INACTIVE")
	void validateLoginEligible_not_active() {
		User user = User.of(1L, USER_UUID, "닉네임", null, "SUSPENDED", LocalDateTime.now(), LocalDateTime.now(), null);

		assertThatThrownBy(() -> validator.validateLoginEligible(user))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_INACTIVE);
	}
}
