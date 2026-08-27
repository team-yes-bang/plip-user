package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.NotificationItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 항목")
public class NotificationItemResponse {

	@Schema(description = "알림 ID")
	private Long id;

	@Schema(description = "알림 유형", example = "CHAT")
	private String type;

	@Schema(description = "제목")
	private String title;

	@Schema(description = "본문")
	private String body;

	@Schema(description = "딥링크", example = "/agit/018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e/chat")
	private String deepLink;

	@Schema(description = "리소스 ID")
	private String resourceId;

	@Schema(description = "아지트 UUID")
	private String agitUuid;

	@Schema(description = "읽음 여부")
	private boolean read;

	@Schema(description = "생성 시각")
	private LocalDateTime createdAt;

	public static NotificationItemResponse of(NotificationItemResult result) {
		NotificationItemResponse response = new NotificationItemResponse();
		response.id = result.getId();
		response.type = result.getType().name();
		response.title = result.getTitle();
		response.body = result.getBody();
		response.deepLink = result.getDeepLink();
		response.resourceId = result.getResourceId();
		response.agitUuid = result.getAgitUuid() == null ? null : result.getAgitUuid().toString();
		response.read = result.isRead();
		response.createdAt = result.getCreatedAt();
		return response;
	}
}
