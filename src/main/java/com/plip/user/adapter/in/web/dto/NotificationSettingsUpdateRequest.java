package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 설정 저장 요청")
public class NotificationSettingsUpdateRequest {

	@NotNull(message = "아지트 알림 허용 여부는 필수입니다.")
	@Schema(description = "아지트 알림 허용 여부", example = "true")
	private Boolean agitNotifyEnabled;

	@NotNull(message = "다이어리 알림 허용 여부는 필수입니다.")
	@Schema(description = "다이어리 알림 허용 여부", example = "true")
	private Boolean diaryNotifyEnabled;

	@NotNull(message = "다이어리 알림 시각은 필수입니다.")
	@Schema(description = "다이어리 알림 시각 (HH:mm:ss)", example = "21:00:00")
	private LocalTime diaryNotifyTime;
}
