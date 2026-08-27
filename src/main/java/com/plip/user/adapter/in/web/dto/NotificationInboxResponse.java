package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.NotificationInboxResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림함")
public class NotificationInboxResponse {

	@Schema(description = "알림 목록")
	private List<NotificationItemResponse> items;

	@Schema(description = "읽지 않은 알림 수")
	private long unreadCount;

	public static NotificationInboxResponse of(NotificationInboxResult result) {
		NotificationInboxResponse response = new NotificationInboxResponse();
		response.items = result.getItems().stream().map(NotificationItemResponse::of).toList();
		response.unreadCount = result.getUnreadCount();
		return response;
	}
}
