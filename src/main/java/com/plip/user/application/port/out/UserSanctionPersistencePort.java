package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserSanction;

import java.util.Optional;

public interface UserSanctionPersistencePort {

	Optional<UserSanction> findById(Long id);

	Optional<String> findActiveSanctionReasonByUserId(Long userId);
}
