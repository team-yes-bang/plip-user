package com.plip.user.application.port.in;

public interface GetNotificationSettingsUseCase {

	NotificationSettingResult getSettings(String userUuid);
}
