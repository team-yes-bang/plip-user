package com.plip.user.application.service;

import com.plip.user.application.port.in.NotificationSettingResult;
import com.plip.user.application.port.in.UpdateNotificationSettingsCommand;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingUpdatedEventPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationSettingsServiceTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final LocalTime NOTIFY_TIME = LocalTime.of(21, 0);

	@InjectMocks
	private UpdateNotificationSettingsService updateNotificationSettingsService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;

	@Mock
	private UserNotificationSettingUpdatedEventPort userNotificationSettingUpdatedEventPort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	private User activeUser;
	private UserNotificationSetting setting;

	@BeforeEach
	void setUp() {
		activeUser = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
		setting = UserNotificationSetting.of(
				10L, 1L, true, true, NOTIFY_TIME, null, null
		);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(activeUser));
	}

	@Test
	@DisplayName("알림 설정 저장 성공 및 Kafka 이벤트 발행")
	void updateSettings_success() {
		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.of(setting));
		given(userNotificationSettingPersistencePort.save(any(UserNotificationSetting.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		NotificationSettingResult result = updateNotificationSettingsService.updateSettings(
				UpdateNotificationSettingsCommand.of(USER_UUID.toString(), false, true, LocalTime.of(9, 30))
		);

		assertThat(result.isAgitNotifyEnabled()).isFalse();
		assertThat(result.isDiaryNotifyEnabled()).isTrue();
		assertThat(result.getDiaryNotifyTime()).isEqualTo(LocalTime.of(9, 30));

		verify(userNotificationSettingUpdatedEventPort).publishSettingUpdated(
				eq(USER_UUID), eq(false), eq(true), eq(LocalTime.of(9, 30))
		);
		verify(userAccountStatusValidator).validateLoginEligible(activeUser);
	}

	@Test
	@DisplayName("알림 설정 없음 시 NOTIFY_001")
	void updateSettings_notFound() {
		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> updateNotificationSettingsService.updateSettings(
				UpdateNotificationSettingsCommand.of(USER_UUID.toString(), true, true, NOTIFY_TIME)
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND);
	}
}
