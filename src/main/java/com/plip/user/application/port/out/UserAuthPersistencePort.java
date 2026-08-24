package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserAuth;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserAuthPersistencePort {

	Optional<UserAuth> findById(Long id);

	Optional<UserAuth> findByEmailAndAuthType(String email, String authType);

	Optional<UserAuth> findByProviderAndProviderUserId(String provider, String providerUserId);

	Optional<UserAuth> findByUserIdAndAuthType(Long userId, String authType);

	Optional<String> findPrimaryEmailByUserId(Long userId);

	UserAuth save(UserAuth userAuth);

	void updatePasswordHash(Long id, String encodedPassword);

	void softDelete(Long id, LocalDateTime deletedAt);
}
