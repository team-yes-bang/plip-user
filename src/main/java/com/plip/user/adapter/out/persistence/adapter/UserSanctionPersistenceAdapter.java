package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserSanctionEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserSanctionRepository;
import com.plip.user.application.port.out.UserSanctionPersistencePort;
import com.plip.user.domain.model.UserSanction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSanctionPersistenceAdapter implements UserSanctionPersistencePort {

	private final UserSanctionRepository userSanctionRepository;
	private final UserSanctionEntityMapper userSanctionEntityMapper;

	@Override
	public Optional<UserSanction> findById(Long id) {
		return userSanctionRepository.findById(id).map(userSanctionEntityMapper::toDomain);
	}

	@Override
	public Optional<String> findActiveSanctionReasonByUserId(Long userId) {
		return userSanctionRepository
				.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE")
				.map(entity -> entity.getReason());
	}
}
