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
}
