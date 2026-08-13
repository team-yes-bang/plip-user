package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserNotificationSetting;

import java.util.Optional;

public interface UserNotificationSettingPersistencePort {

	Optional<UserNotificationSetting> findById(Long id);

	UserNotificationSetting save(UserNotificationSetting setting);
}
