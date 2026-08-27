package com.plip.user.application.port.in;

import com.plip.user.domain.model.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationItemResult {

	private Long id;
	private NotificationType type;
	private String title;
	private String body;
	private String deepLink;
	private String resourceId;
	private UUID agitUuid;
	private boolean read;
	private LocalDateTime createdAt;

	public static NotificationItemResult of(
			Long id,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			UUID agitUuid,
			boolean read,
			LocalDateTime createdAt
	) {
		NotificationItemResult result = new NotificationItemResult();
		result.id = id;
		result.type = type;
		result.title = title;
		result.body = body;
		result.deepLink = deepLink;
		result.resourceId = resourceId;
		result.agitUuid = agitUuid;
		result.read = read;
		result.createdAt = createdAt;
		return result;
	}
}
