package com.plip.user.application.service;

import com.plip.user.application.port.in.NotificationSettingResult;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserNotificationSetting;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetNotificationSettingsServiceTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@InjectMocks
	private GetNotificationSettingsService getNotificationSettingsService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	@BeforeEach
	void setUp() {
		User activeUser = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(activeUser));
	}

	@Test
	@DisplayName("알림 설정 조회 성공")
	void getSettings_success() {
		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.of(
				UserNotificationSetting.of(10L, 1L, true, false, LocalTime.of(21, 0), null, null)
		));

		NotificationSettingResult result = getNotificationSettingsService.getSettings(USER_UUID.toString());

		assertThat(result.isAgitNotifyEnabled()).isTrue();
		assertThat(result.isDiaryNotifyEnabled()).isFalse();
		assertThat(result.getDiaryNotifyTime()).isEqualTo(LocalTime.of(21, 0));
		verify(userAccountStatusValidator).validateLoginEligible(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("알림 설정 없음 시 NOTIFY_001")
	void getSettings_notFound() {
		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> getNotificationSettingsService.getSettings(USER_UUID.toString()))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND);
	}
}
