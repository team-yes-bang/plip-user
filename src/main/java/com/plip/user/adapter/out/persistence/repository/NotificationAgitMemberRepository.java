package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.NotificationAgitMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationAgitMemberRepository extends JpaRepository<NotificationAgitMemberEntity, Long> {

	Optional<NotificationAgitMemberEntity> findByAgitUuidAndUserUuid(UUID agitUuid, UUID userUuid);

	List<NotificationAgitMemberEntity> findByAgitUuidAndActiveIsTrue(UUID agitUuid);

	Optional<NotificationAgitMemberEntity> findFirstByAgitUuidAndActiveIsTrueAndRoleIgnoreCase(
			UUID agitUuid,
			String role
	);
}
