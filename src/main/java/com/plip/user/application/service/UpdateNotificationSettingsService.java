package com.plip.user.application.service;

import com.plip.user.application.port.in.NotificationSettingResult;
import com.plip.user.application.port.in.UpdateNotificationSettingsCommand;
import com.plip.user.application.port.in.UpdateNotificationSettingsUseCase;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingUpdatedEventPort;
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
public class UpdateNotificationSettingsService implements UpdateNotificationSettingsUseCase {

	private final UserPersistencePort userPersistencePort;
	private final UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;
	private final UserNotificationSettingUpdatedEventPort userNotificationSettingUpdatedEventPort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public NotificationSettingResult updateSettings(UpdateNotificationSettingsCommand command) {
		if (command.isEmpty()) {
			throw new BusinessException(ErrorCode.NOTIFICATION_SETTINGS_UPDATE_EMPTY);
		}

		UuidV7 userUuid = UuidV7.parse(command.getUserUuid());
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		UserNotificationSetting setting = userNotificationSettingPersistencePort.findByUserId(user.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));

		setting.updatePartial(
				command.hasAgitNotifyEnabled() ? command.getAgitNotifyEnabled() : null,
				command.hasDiaryNotifyEnabled() ? command.getDiaryNotifyEnabled() : null,
				command.hasDiaryNotifyTime() ? command.getDiaryNotifyTime() : null
		);

		UserNotificationSetting saved = userNotificationSettingPersistencePort.save(setting);

		userNotificationSettingUpdatedEventPort.publishSettingUpdated(
				userUuid,
				saved.isAgitNotifyEnabled(),
				saved.isDiaryNotifyEnabled(),
				saved.getDiaryNotifyTime()
		);

		return NotificationSettingResult.of(
				saved.isAgitNotifyEnabled(),
				saved.isDiaryNotifyEnabled(),
				saved.getDiaryNotifyTime()
		);
	}
}
