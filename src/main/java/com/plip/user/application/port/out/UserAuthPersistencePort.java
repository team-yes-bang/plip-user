package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserAuth;

import java.util.Optional;

public interface UserAuthPersistencePort {

	Optional<UserAuth> findById(Long id);
}
