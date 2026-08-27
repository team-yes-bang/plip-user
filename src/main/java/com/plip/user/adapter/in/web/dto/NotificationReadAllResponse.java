package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "모두 읽음 결과")
public class NotificationReadAllResponse {

	@Schema(description = "읽음 처리된 알림 수")
	private long updatedCount;

	public static NotificationReadAllResponse of(long updatedCount) {
		NotificationReadAllResponse response = new NotificationReadAllResponse();
		response.updatedCount = updatedCount;
		return response;
	}
}
