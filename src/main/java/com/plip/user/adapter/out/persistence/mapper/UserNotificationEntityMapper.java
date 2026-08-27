package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserNotificationEntity;
import com.plip.user.domain.model.UserNotification;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationEntityMapper {

	public UserNotification toDomain(UserNotificationEntity entity) {
		return UserNotification.of(
				entity.getId(),
				entity.getRecipientUserUuid(),
				entity.getType(),
				entity.getTitle(),
				entity.getBody(),
				entity.getDeepLink(),
				entity.getResourceId(),
				entity.getAgitUuid(),
				entity.getDedupeKey(),
				entity.getReadAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public UserNotificationEntity toEntity(UserNotification notification) {
		return UserNotificationEntity.builder()
				.id(notification.getId())
				.recipientUserUuid(notification.getRecipientUserUuid())
				.type(notification.getType())
				.title(notification.getTitle())
				.body(notification.getBody())
				.deepLink(notification.getDeepLink())
				.resourceId(notification.getResourceId())
				.agitUuid(notification.getAgitUuid())
				.dedupeKey(notification.getDedupeKey())
				.readAt(notification.getReadAt())
				.build();
	}
}
