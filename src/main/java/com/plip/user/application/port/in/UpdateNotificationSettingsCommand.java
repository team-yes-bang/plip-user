package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNotificationSettingsCommand {

	private String userUuid;
	private Boolean agitNotifyEnabled;
	private Boolean diaryNotifyEnabled;
	private LocalTime diaryNotifyTime;

	private UpdateNotificationSettingsCommand(
			String userUuid,
			Boolean agitNotifyEnabled,
			Boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		this.userUuid = userUuid;
		this.agitNotifyEnabled = agitNotifyEnabled;
		this.diaryNotifyEnabled = diaryNotifyEnabled;
		this.diaryNotifyTime = diaryNotifyTime;
	}

	public static UpdateNotificationSettingsCommand of(
			String userUuid,
			Boolean agitNotifyEnabled,
			Boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		return new UpdateNotificationSettingsCommand(
				userUuid, agitNotifyEnabled, diaryNotifyEnabled, diaryNotifyTime
		);
	}

	public boolean hasAgitNotifyEnabled() {
		return agitNotifyEnabled != null;
	}

	public boolean hasDiaryNotifyEnabled() {
		return diaryNotifyEnabled != null;
	}

	public boolean hasDiaryNotifyTime() {
		return diaryNotifyTime != null;
	}

	public boolean isEmpty() {
		return !hasAgitNotifyEnabled() && !hasDiaryNotifyEnabled() && !hasDiaryNotifyTime();
	}
}
