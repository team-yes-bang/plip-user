package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserAuthEntity;
import com.plip.user.domain.model.UserAuth;
import org.springframework.stereotype.Component;

@Component
public class UserAuthEntityMapper {

	public UserAuth toDomain(UserAuthEntity entity) {
		return UserAuth.of(
				entity.getId(),
				entity.getUserId(),
				entity.getAuthType(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getProvider(),
				entity.getProviderUserId(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}

	public UserAuthEntity toEntity(UserAuth userAuth) {
		return UserAuthEntity.builder()
				.id(userAuth.getId())
				.userId(userAuth.getUserId())
				.authType(userAuth.getAuthType())
				.email(userAuth.getEmail())
				.passwordHash(userAuth.getPasswordHash())
				.provider(userAuth.getProvider())
				.providerUserId(userAuth.getProviderUserId())
				.build();
	}
}
