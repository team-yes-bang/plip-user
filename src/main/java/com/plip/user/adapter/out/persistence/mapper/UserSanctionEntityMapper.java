package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserSanctionEntity;
import com.plip.user.domain.model.UserSanction;
import com.plip.user.domain.model.UuidV7;
import org.springframework.stereotype.Component;

@Component
public class UserSanctionEntityMapper {

	public UserSanction toDomain(UserSanctionEntity entity) {
		return UserSanction.of(
				entity.getId(),
				entity.getUserId(),
				UuidV7.of(entity.getAdminUuid()),
				entity.getSanctionType(),
				entity.getDurationDays(),
				entity.getStartDate(),
				entity.getEndDate(),
				entity.getStatus(),
				entity.getReason(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
