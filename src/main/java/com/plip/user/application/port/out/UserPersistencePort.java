package com.plip.user.application.port.out;

import com.plip.user.domain.model.User;

import java.util.Optional;

public interface UserPersistencePort {

	Optional<User> findById(Long id);

	User save(User user);
}
