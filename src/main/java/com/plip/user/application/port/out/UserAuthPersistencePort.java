package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserAuth;

import java.util.Optional;

public interface UserAuthPersistencePort {

	Optional<UserAuth> findById(Long id);

	Optional<UserAuth> findByEmailAndAuthType(String email, String authType);

	Optional<UserAuth> findByProviderAndProviderUserId(String provider, String providerUserId);

	UserAuth save(UserAuth userAuth);
}
