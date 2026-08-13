package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserNotificationSettingEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserNotificationSettingRepository;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.domain.model.UserNotificationSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserNotificationSettingPersistenceAdapter implements UserNotificationSettingPersistencePort {

	private final UserNotificationSettingRepository userNotificationSettingRepository;
	private final UserNotificationSettingEntityMapper userNotificationSettingEntityMapper;

	@Override
	public Optional<UserNotificationSetting> findById(Long id) {
		return userNotificationSettingRepository.findById(id).map(userNotificationSettingEntityMapper::toDomain);
	}

	@Override
	public UserNotificationSetting save(UserNotificationSetting setting) {
		return userNotificationSettingEntityMapper.toDomain(
				userNotificationSettingRepository.save(
						userNotificationSettingEntityMapper.toEntity(setting)
				)
		);
	}
}
