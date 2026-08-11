package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserRepository;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

	private final UserRepository userRepository;
	private final UserEntityMapper userEntityMapper;

	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id).map(userEntityMapper::toDomain);
	}

	@Override
	public User save(User user) {
		return userEntityMapper.toDomain(userRepository.save(userEntityMapper.toEntity(user)));
	}
}
