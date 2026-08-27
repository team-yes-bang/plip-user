package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserNotificationEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserNotificationRepository;
import com.plip.user.application.port.out.UserNotificationPersistencePort;
import com.plip.user.domain.model.UserNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserNotificationPersistenceAdapter implements UserNotificationPersistencePort {

	private final UserNotificationRepository userNotificationRepository;
	private final UserNotificationEntityMapper userNotificationEntityMapper;

	@Override
	public UserNotification save(UserNotification notification) {
		return userNotificationEntityMapper.toDomain(
				userNotificationRepository.save(userNotificationEntityMapper.toEntity(notification))
		);
	}

	@Override
	public Optional<UserNotification> findByIdAndRecipient(Long id, UUID recipientUserUuid) {
		return userNotificationRepository.findByIdAndRecipientUserUuid(id, recipientUserUuid)
				.map(userNotificationEntityMapper::toDomain);
	}

	@Override
	public List<UserNotification> findRecentByRecipient(UUID recipientUserUuid, int limit) {
		return userNotificationRepository
				.findByRecipientUserUuidOrderByCreatedAtDesc(recipientUserUuid, PageRequest.of(0, limit))
				.stream()
				.map(userNotificationEntityMapper::toDomain)
				.toList();
	}

	@Override
	public long countUnreadByRecipient(UUID recipientUserUuid) {
		return userNotificationRepository.countByRecipientUserUuidAndReadAtIsNull(recipientUserUuid);
	}

	@Override
	public boolean existsByRecipientAndDedupeKey(UUID recipientUserUuid, String dedupeKey) {
		return userNotificationRepository.existsByRecipientUserUuidAndDedupeKey(recipientUserUuid, dedupeKey);
	}

	@Override
	public int markAllRead(UUID recipientUserUuid) {
		return userNotificationRepository.markAllRead(recipientUserUuid, LocalDateTime.now());
	}
}
