package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.NotificationAgitMemberEntity;
import com.plip.user.domain.model.NotificationAgitMember;
import org.springframework.stereotype.Component;

@Component
public class NotificationAgitMemberEntityMapper {

	public NotificationAgitMember toDomain(NotificationAgitMemberEntity entity) {
		return NotificationAgitMember.of(
				entity.getId(),
				entity.getAgitUuid(),
				entity.getUserUuid(),
				entity.getRole(),
				entity.isActive(),
				entity.getAgitName()
		);
	}

	public NotificationAgitMemberEntity toEntity(NotificationAgitMember member) {
		return NotificationAgitMemberEntity.builder()
				.id(member.getId())
				.agitUuid(member.getAgitUuid())
				.userUuid(member.getUserUuid())
				.role(member.getRole())
				.active(member.isActive())
				.agitName(member.getAgitName())
				.build();
	}
}
