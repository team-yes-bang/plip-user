package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationSettingResult {

	private boolean agitNotifyEnabled;
	private boolean diaryNotifyEnabled;
	private LocalTime diaryNotifyTime;

	private NotificationSettingResult(boolean agitNotifyEnabled, boolean diaryNotifyEnabled, LocalTime diaryNotifyTime) {
		this.agitNotifyEnabled = agitNotifyEnabled;
		this.diaryNotifyEnabled = diaryNotifyEnabled;
		this.diaryNotifyTime = diaryNotifyTime;
	}

	public static NotificationSettingResult of(
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		return new NotificationSettingResult(agitNotifyEnabled, diaryNotifyEnabled, diaryNotifyTime);
	}
}
