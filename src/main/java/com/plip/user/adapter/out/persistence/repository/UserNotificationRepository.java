package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.UserNotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, Long> {

	List<UserNotificationEntity> findByRecipientUserUuidOrderByCreatedAtDesc(UUID recipientUserUuid, Pageable pageable);

	long countByRecipientUserUuidAndReadAtIsNull(UUID recipientUserUuid);

	Optional<UserNotificationEntity> findByIdAndRecipientUserUuid(Long id, UUID recipientUserUuid);

	boolean existsByRecipientUserUuidAndDedupeKey(UUID recipientUserUuid, String dedupeKey);

	@Modifying
	@Query("update UserNotificationEntity n set n.readAt = :now "
			+ "where n.recipientUserUuid = :recipient and n.readAt is null")
	int markAllRead(@Param("recipient") UUID recipient, @Param("now") LocalDateTime now);
}
