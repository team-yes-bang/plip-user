package com.plip.user.application.port.out;

import com.plip.user.domain.model.UuidV7;

import java.time.LocalTime;

public interface UserNotificationSettingUpdatedEventPort {

	void publishSettingUpdated(
			UuidV7 userUuid,
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	);
}
