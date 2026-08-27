package com.plip.user.application.port.out;

import com.plip.user.domain.model.NotificationAgitMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationAgitMemberPersistencePort {

	NotificationAgitMember save(NotificationAgitMember member);

	Optional<NotificationAgitMember> findByAgitUuidAndUserUuid(UUID agitUuid, UUID userUuid);

	List<NotificationAgitMember> findActiveByAgitUuid(UUID agitUuid);

	Optional<NotificationAgitMember> findActiveHost(UUID agitUuid);
}
