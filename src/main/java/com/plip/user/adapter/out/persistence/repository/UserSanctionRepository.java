package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.UserSanctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSanctionRepository extends JpaRepository<UserSanctionEntity, Long> {

	Optional<UserSanctionEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
