package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNotificationSettingsCommand {

	private String userUuid;
	private boolean agitNotifyEnabled;
	private boolean diaryNotifyEnabled;
	private LocalTime diaryNotifyTime;

	private UpdateNotificationSettingsCommand(
			String userUuid,
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		this.userUuid = userUuid;
		this.agitNotifyEnabled = agitNotifyEnabled;
		this.diaryNotifyEnabled = diaryNotifyEnabled;
		this.diaryNotifyTime = diaryNotifyTime;
	}

	public static UpdateNotificationSettingsCommand of(
			String userUuid,
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		return new UpdateNotificationSettingsCommand(
				userUuid, agitNotifyEnabled, diaryNotifyEnabled, diaryNotifyTime
		);
	}
}
