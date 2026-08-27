package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationPersistencePort {

	UserNotification save(UserNotification notification);

	Optional<UserNotification> findByIdAndRecipient(Long id, UUID recipientUserUuid);

	List<UserNotification> findRecentByRecipient(UUID recipientUserUuid, int limit);

	long countUnreadByRecipient(UUID recipientUserUuid);

	boolean existsByRecipientAndDedupeKey(UUID recipientUserUuid, String dedupeKey);

	int markAllRead(UUID recipientUserUuid);
}
