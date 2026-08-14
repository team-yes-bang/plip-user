package com.plip.user.application.port.out;

import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;

import java.util.Optional;

public interface UserPersistencePort {

	Optional<User> findById(Long id);

	Optional<User> findByUserUuid(UuidV7 userUuid);

	User save(User user);
}
