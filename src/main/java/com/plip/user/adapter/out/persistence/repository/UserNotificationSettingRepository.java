package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.UserNotificationSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSettingEntity, Long> {

	Optional<UserNotificationSettingEntity> findByUserId(Long userId);
}
