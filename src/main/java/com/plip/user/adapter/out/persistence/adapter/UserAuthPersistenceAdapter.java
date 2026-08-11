package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserAuthEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserAuthRepository;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.domain.model.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthPersistenceAdapter implements UserAuthPersistencePort {

	private final UserAuthRepository userAuthRepository;
	private final UserAuthEntityMapper userAuthEntityMapper;

	@Override
	public Optional<UserAuth> findById(Long id) {
		return userAuthRepository.findById(id).map(userAuthEntityMapper::toDomain);
	}
}
