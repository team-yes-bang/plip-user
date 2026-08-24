package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 설정 수정 요청 (전달한 필드만 수정)")
public class NotificationSettingsPatchRequest {

	@Schema(description = "아지트 알림 허용 여부 (선택). 토글 변경 시 해당 필드만 전달", example = "true")
	private Boolean agitNotifyEnabled;

	@Schema(description = "다이어리 알림 허용 여부 (선택). 토글 변경 시 해당 필드만 전달", example = "true")
	private Boolean diaryNotifyEnabled;

	@Schema(description = "다이어리 알림 시각 (선택). 저장 버튼 클릭 시 해당 필드만 전달", example = "21:00:00")
	private LocalTime diaryNotifyTime;
}
