package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationSetting {

	private Long id;
	private Long userId;
	private boolean agitNotifyEnabled;
	private boolean diaryNotifyEnabled;
	private LocalTime diaryNotifyTime;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static UserNotificationSetting of(
			Long id,
			Long userId,
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		UserNotificationSetting setting = new UserNotificationSetting();
		setting.id = id;
		setting.userId = userId;
		setting.agitNotifyEnabled = agitNotifyEnabled;
		setting.diaryNotifyEnabled = diaryNotifyEnabled;
		setting.diaryNotifyTime = diaryNotifyTime;
		setting.createdAt = createdAt;
		setting.updatedAt = updatedAt;
		return setting;
	}

	public void update(boolean agitNotifyEnabled, boolean diaryNotifyEnabled, LocalTime diaryNotifyTime) {
		this.agitNotifyEnabled = agitNotifyEnabled;
		this.diaryNotifyEnabled = diaryNotifyEnabled;
		this.diaryNotifyTime = diaryNotifyTime;
	}

	public void updatePartial(Boolean agitNotifyEnabled, Boolean diaryNotifyEnabled, LocalTime diaryNotifyTime) {
		if (agitNotifyEnabled != null) {
			this.agitNotifyEnabled = agitNotifyEnabled;
		}
		if (diaryNotifyEnabled != null) {
			this.diaryNotifyEnabled = diaryNotifyEnabled;
		}
		if (diaryNotifyTime != null) {
			this.diaryNotifyTime = diaryNotifyTime;
		}
	}
}
