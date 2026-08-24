package com.plip.user.application.service;

import com.plip.user.application.port.in.GetNotificationSettingsUseCase;
import com.plip.user.application.port.in.NotificationSettingResult;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserNotificationSetting;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetNotificationSettingsService implements GetNotificationSettingsUseCase {

	private final UserPersistencePort userPersistencePort;
	private final UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	public NotificationSettingResult getSettings(String userUuidValue) {
		UuidV7 userUuid = UuidV7.parse(userUuidValue);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		UserNotificationSetting setting = userNotificationSettingPersistencePort.findByUserId(user.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));

		return NotificationSettingResult.of(
				setting.isAgitNotifyEnabled(),
				setting.isDiaryNotifyEnabled(),
				setting.getDiaryNotifyTime()
		);
	}
}
