package com.plip.user.application.port.in;

import com.plip.user.domain.model.NotificationType;

import java.util.UUID;

public record IngestNotificationCommand(
		UUID recipientUserUuid,
		NotificationType type,
		String title,
		String body,
		String deepLink,
		String resourceId,
		UUID agitUuid,
		String dedupeKey
) {
}
