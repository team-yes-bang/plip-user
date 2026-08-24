package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.NotificationSettingResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 설정 응답")
public class NotificationSettingsResponse {

	@Schema(description = "아지트 알림 허용 여부")
	private boolean agitNotifyEnabled;

	@Schema(description = "다이어리 알림 허용 여부")
	private boolean diaryNotifyEnabled;

	@Schema(description = "다이어리 알림 시각")
	private LocalTime diaryNotifyTime;

	private NotificationSettingsResponse(
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		this.agitNotifyEnabled = agitNotifyEnabled;
		this.diaryNotifyEnabled = diaryNotifyEnabled;
		this.diaryNotifyTime = diaryNotifyTime;
	}

	public static NotificationSettingsResponse of(NotificationSettingResult result) {
		return new NotificationSettingsResponse(
				result.isAgitNotifyEnabled(),
				result.isDiaryNotifyEnabled(),
				result.getDiaryNotifyTime()
		);
	}
}
