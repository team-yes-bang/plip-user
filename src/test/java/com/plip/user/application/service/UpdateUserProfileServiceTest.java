package com.plip.user.application.service;

import com.plip.user.application.port.in.UpdateUserProfileCommand;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileServiceTest {

	@InjectMocks
	private UpdateUserProfileService updateUserProfileService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private UserAuthPersistencePort userAuthPersistencePort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("닉네임 수정 성공")
	void updateProfile_nickname() {
		User user = activeUser(null);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(userAuthPersistencePort.findPrimaryEmailByUserId(1L)).willReturn(Optional.of("user@example.com"));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.empty());

		UserProfileResult result = updateUserProfileService.updateProfile(
				UpdateUserProfileCommand.of(USER_UUID.toString(), "새닉네임", null)
		);

		assertThat(result.getNickname()).isEqualTo("새닉네임");
	}

	@Test
	@DisplayName("프로필 이미지 경로 수정 성공")
	void updateProfile_profileImagePath() {
		User user = activeUser(null);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(userAuthPersistencePort.findPrimaryEmailByUserId(1L)).willReturn(Optional.of("user@example.com"));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.empty());

		UserProfileResult result = updateUserProfileService.updateProfile(
				UpdateUserProfileCommand.of(USER_UUID.toString(), null, "profiles/user.png")
		);

		assertThat(result.getProfileImagePath()).isEqualTo("profiles/user.png");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userPersistencePort).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getProfileImagePath()).isEqualTo("profiles/user.png");
	}

	@Test
	@DisplayName("프로필 이미지 경로 빈 문자열이면 제거")
	void updateProfile_clearProfileImagePath() {
		User user = activeUser("profiles/old.png");

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(userAuthPersistencePort.findPrimaryEmailByUserId(1L)).willReturn(Optional.of("user@example.com"));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.empty());

		UserProfileResult result = updateUserProfileService.updateProfile(
				UpdateUserProfileCommand.of(USER_UUID.toString(), null, "")
		);

		assertThat(result.getProfileImagePath()).isNull();
	}

	@Test
	@DisplayName("수정 항목 없음")
	void updateProfile_empty() {
		assertThatThrownBy(() -> updateUserProfileService.updateProfile(
				UpdateUserProfileCommand.of(USER_UUID.toString(), null, null)
		))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.PROFILE_UPDATE_EMPTY);
	}

	@Test
	@DisplayName("닉네임 형식 오류")
	void updateProfile_invalidNickname() {
		User user = activeUser(null);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> updateUserProfileService.updateProfile(
				UpdateUserProfileCommand.of(USER_UUID.toString(), "a", null)
		))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_NICKNAME);
	}

	private User activeUser(String profileImagePath) {
		return User.of(1L, USER_UUID, "플립이", profileImagePath, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
	}
}
