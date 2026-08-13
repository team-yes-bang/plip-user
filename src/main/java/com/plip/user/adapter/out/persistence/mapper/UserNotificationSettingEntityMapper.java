package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserNotificationSettingEntity;
import com.plip.user.domain.model.UserNotificationSetting;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationSettingEntityMapper {

	public UserNotificationSetting toDomain(UserNotificationSettingEntity entity) {
		return UserNotificationSetting.of(
				entity.getId(),
				entity.getUserId(),
				entity.isAgitNotifyEnabled(),
				entity.isDiaryNotifyEnabled(),
				entity.getDiaryNotifyTime(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public UserNotificationSettingEntity toEntity(UserNotificationSetting setting) {
		return UserNotificationSettingEntity.builder()
				.userId(setting.getUserId())
				.agitNotifyEnabled(setting.isAgitNotifyEnabled())
				.diaryNotifyEnabled(setting.isDiaryNotifyEnabled())
				.diaryNotifyTime(setting.getDiaryNotifyTime())
				.build();
	}
}
