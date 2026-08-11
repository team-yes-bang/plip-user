package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserEntity;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

	public User toDomain(UserEntity entity) {
		return User.of(
				entity.getId(),
				UuidV7.of(entity.getUserUuid()),
				entity.getNickname(),
				entity.getProfileImagePath(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}

	public UserEntity toEntity(User user) {
		return UserEntity.builder()
				.userUuid(user.getUserUuid().toUuid())
				.nickname(user.getNickname())
				.profileImagePath(user.getProfileImagePath())
				.status(user.getStatus())
				.build();
	}
}
