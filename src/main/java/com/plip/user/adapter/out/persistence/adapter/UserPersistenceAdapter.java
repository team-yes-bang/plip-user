package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.entity.UserEntity;
import com.plip.user.adapter.out.persistence.mapper.UserEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserRepository;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.domain.model.User;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
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
	public Optional<User> findByUserUuid(UuidV7 userUuid) {
		return userRepository.findByUserUuid(userUuid.toUuid())
				.map(userEntityMapper::toDomain);
	}

	@Override
	public User save(User user) {
		UserEntity entity;
		if (user.getId() != null) {
			entity = userRepository.findById(user.getId())
					.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
			entity.applyFromDomain(user);
		} else {
			entity = userEntityMapper.toNewEntity(user);
		}
		return userEntityMapper.toDomain(userRepository.save(entity));
	}
}
