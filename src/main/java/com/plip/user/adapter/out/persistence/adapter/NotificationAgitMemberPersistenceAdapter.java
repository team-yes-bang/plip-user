package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.NotificationAgitMemberEntityMapper;
import com.plip.user.adapter.out.persistence.repository.NotificationAgitMemberRepository;
import com.plip.user.application.port.out.NotificationAgitMemberPersistencePort;
import com.plip.user.domain.model.NotificationAgitMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationAgitMemberPersistenceAdapter implements NotificationAgitMemberPersistencePort {

	private final NotificationAgitMemberRepository notificationAgitMemberRepository;
	private final NotificationAgitMemberEntityMapper notificationAgitMemberEntityMapper;

	@Override
	public NotificationAgitMember save(NotificationAgitMember member) {
		return notificationAgitMemberEntityMapper.toDomain(
				notificationAgitMemberRepository.save(notificationAgitMemberEntityMapper.toEntity(member))
		);
	}

	@Override
	public Optional<NotificationAgitMember> findByAgitUuidAndUserUuid(UUID agitUuid, UUID userUuid) {
		return notificationAgitMemberRepository.findByAgitUuidAndUserUuid(agitUuid, userUuid)
				.map(notificationAgitMemberEntityMapper::toDomain);
	}

	@Override
	public List<NotificationAgitMember> findActiveByAgitUuid(UUID agitUuid) {
		return notificationAgitMemberRepository.findByAgitUuidAndActiveIsTrue(agitUuid).stream()
				.map(notificationAgitMemberEntityMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<NotificationAgitMember> findActiveHost(UUID agitUuid) {
		return notificationAgitMemberRepository
				.findFirstByAgitUuidAndActiveIsTrueAndRoleIgnoreCase(agitUuid, "HOST")
				.map(notificationAgitMemberEntityMapper::toDomain);
	}
}
