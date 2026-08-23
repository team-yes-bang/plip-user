package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.UserAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {

	Optional<UserAuthEntity> findByEmailAndAuthTypeAndDeletedAtIsNull(String email, String authType);

	Optional<UserAuthEntity> findByProviderAndProviderUserIdAndDeletedAtIsNull(
			String provider, String providerUserId);

	Optional<UserAuthEntity> findByUserIdAndAuthTypeAndDeletedAtIsNull(Long userId, String authType);

	List<UserAuthEntity> findByUserIdAndDeletedAtIsNull(Long userId);
}
