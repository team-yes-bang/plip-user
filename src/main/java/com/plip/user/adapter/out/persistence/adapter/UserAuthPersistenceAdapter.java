package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.entity.UserAuthEntity;
import com.plip.user.adapter.out.persistence.mapper.UserAuthEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserAuthRepository;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.domain.model.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthPersistenceAdapter implements UserAuthPersistencePort {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserAuthRepository userAuthRepository;
	private final UserAuthEntityMapper userAuthEntityMapper;

	@Override
	public Optional<UserAuth> findById(Long id) {
		return userAuthRepository.findById(id).map(userAuthEntityMapper::toDomain);
	}

	@Override
	public Optional<UserAuth> findByEmailAndAuthType(String email, String authType) {
		return userAuthRepository.findByEmailAndAuthTypeAndDeletedAtIsNull(email, authType)
				.map(userAuthEntityMapper::toDomain);
	}

	@Override
	public Optional<UserAuth> findByProviderAndProviderUserId(String provider, String providerUserId) {
		return userAuthRepository.findByProviderAndProviderUserIdAndDeletedAtIsNull(provider, providerUserId)
				.map(userAuthEntityMapper::toDomain);
	}

	@Override
	public Optional<UserAuth> findByUserIdAndAuthType(Long userId, String authType) {
		return userAuthRepository.findByUserIdAndAuthTypeAndDeletedAtIsNull(userId, authType)
				.map(userAuthEntityMapper::toDomain);
	}

	@Override
	public Optional<String> findPrimaryEmailByUserId(Long userId) {
		Optional<UserAuth> localAuth = findByUserIdAndAuthType(userId, AUTH_TYPE_LOCAL);
		if (localAuth.map(UserAuth::getEmail).filter(StringUtils::hasText).isPresent()) {
			return Optional.of(localAuth.get().getEmail());
		}

		return userAuthRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
				.map(UserAuthEntity::getEmail)
				.filter(StringUtils::hasText)
				.findFirst()
				.map(String::trim);
	}

	@Override
	public UserAuth save(UserAuth userAuth) {
		return userAuthEntityMapper.toDomain(
				userAuthRepository.save(userAuthEntityMapper.toEntity(userAuth))
		);
	}

	@Override
	public void updatePasswordHash(Long id, String encodedPassword) {
		UserAuthEntity entity = userAuthRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException("UserAuth not found: " + id));
		entity.updatePasswordHash(encodedPassword);
	}
}
