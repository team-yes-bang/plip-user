package com.plip.user.application.service;

import com.plip.user.application.port.in.UserProfileResult;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetUserProfileServiceTest {

	@InjectMocks
	private GetUserProfileService getUserProfileService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private UserAuthPersistencePort userAuthPersistencePort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("프로필 조회 성공")
	void getProfile_success() {
		User user = User.of(1L, USER_UUID, "플립이", "/users/avatar.jpg", "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userAuthPersistencePort.findPrimaryEmailByUserId(1L)).willReturn(Optional.of("user@example.com"));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.empty());

		UserProfileResult result = getUserProfileService.getProfile(USER_UUID.toString());

		assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
		assertThat(result.getNickname()).isEqualTo("플립이");
		assertThat(result.getProfileImagePath()).isEqualTo("/users/avatar.jpg");
		assertThat(result.getEmail()).isEqualTo("user@example.com");
		assertThat(result.isHasLocalAuth()).isFalse();
	}

	@Test
	@DisplayName("사용자 없음")
	void getProfile_userNotFound() {
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> getUserProfileService.getProfile(USER_UUID.toString()))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.USER_NOT_FOUND);
	}
}
