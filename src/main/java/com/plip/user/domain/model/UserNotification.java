package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification {

	private Long id;
	private UUID recipientUserUuid;
	private NotificationType type;
	private String title;
	private String body;
	private String deepLink;
	private String resourceId;
	private UUID agitUuid;
	private String dedupeKey;
	private LocalDateTime readAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static UserNotification of(
			Long id,
			UUID recipientUserUuid,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			UUID agitUuid,
			String dedupeKey,
			LocalDateTime readAt,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		UserNotification notification = new UserNotification();
		notification.id = id;
		notification.recipientUserUuid = recipientUserUuid;
		notification.type = type;
		notification.title = title;
		notification.body = body;
		notification.deepLink = deepLink;
		notification.resourceId = resourceId;
		notification.agitUuid = agitUuid;
		notification.dedupeKey = dedupeKey;
		notification.readAt = readAt;
		notification.createdAt = createdAt;
		notification.updatedAt = updatedAt;
		return notification;
	}

	public static UserNotification create(
			UUID recipientUserUuid,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			UUID agitUuid,
			String dedupeKey
	) {
		return of(
				null,
				recipientUserUuid,
				type,
				title,
				body,
				deepLink,
				resourceId,
				agitUuid,
				dedupeKey,
				null,
				null,
				null
		);
	}

	public boolean isRead() {
		return readAt != null;
	}

	public void markRead(LocalDateTime now) {
		if (this.readAt == null) {
			this.readAt = now;
		}
	}
}
