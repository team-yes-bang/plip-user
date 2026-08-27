package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "읽지 않은 알림 수")
public class NotificationUnreadCountResponse {

	@Schema(description = "읽지 않은 알림 수")
	private long unreadCount;

	public static NotificationUnreadCountResponse of(long unreadCount) {
		NotificationUnreadCountResponse response = new NotificationUnreadCountResponse();
		response.unreadCount = unreadCount;
		return response;
	}
}
