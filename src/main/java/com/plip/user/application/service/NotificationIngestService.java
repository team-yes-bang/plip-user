package com.plip.user.application.service;

import com.plip.user.application.port.in.IngestNotificationCommand;
import com.plip.user.application.port.out.NotificationAgitMemberPersistencePort;
import com.plip.user.application.port.out.UserNotificationPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.NotificationAgitMember;
import com.plip.user.domain.model.NotificationType;
import com.plip.user.domain.model.UserNotification;
import com.plip.user.domain.model.UserNotificationSetting;
import com.plip.user.domain.model.UuidV7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationIngestService {

	private final UserNotificationPersistencePort userNotificationPersistencePort;
	private final NotificationAgitMemberPersistencePort notificationAgitMemberPersistencePort;
	private final UserPersistencePort userPersistencePort;
	private final UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;

	@Transactional
	public void ingest(IngestNotificationCommand command) {
		if (command == null || command.recipientUserUuid() == null || command.type() == null
				|| command.dedupeKey() == null || command.dedupeKey().isBlank()
				|| command.title() == null || command.title().isBlank()) {
			return;
		}
		if (userNotificationPersistencePort.existsByRecipientAndDedupeKey(
				command.recipientUserUuid(), command.dedupeKey())) {
			return;
		}
		if (!isEnabled(command.recipientUserUuid(), command.type())) {
			return;
		}
		userNotificationPersistencePort.save(UserNotification.create(
				command.recipientUserUuid(),
				command.type(),
				command.title(),
				command.body(),
				command.deepLink(),
				command.resourceId(),
				command.agitUuid(),
				command.dedupeKey()
		));
	}

	@Transactional
	public void upsertMember(UUID agitUuid, UUID userUuid, String role, boolean active, String agitName) {
		if (agitUuid == null || userUuid == null) {
			return;
		}
		NotificationAgitMember member = notificationAgitMemberPersistencePort
				.findByAgitUuidAndUserUuid(agitUuid, userUuid)
				.orElseGet(() -> NotificationAgitMember.of(null, agitUuid, userUuid,
						role == null ? "GUEST" : role, active, agitName));
		if (active) {
			member.activate(role, agitName);
		} else {
			member.deactivate();
		}
		if (role != null) {
			member.updateRole(role);
		}
		notificationAgitMemberPersistencePort.save(member);
	}

	@Transactional(readOnly = true)
	public List<UUID> activeMemberUuids(UUID agitUuid) {
		if (agitUuid == null) {
			return List.of();
		}
		return notificationAgitMemberPersistencePort.findActiveByAgitUuid(agitUuid).stream()
				.map(NotificationAgitMember::getUserUuid)
				.toList();
	}

	@Transactional(readOnly = true)
	public UUID hostUserUuid(UUID agitUuid) {
		if (agitUuid == null) {
			return null;
		}
		return notificationAgitMemberPersistencePort.findActiveHost(agitUuid)
				.map(NotificationAgitMember::getUserUuid)
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public String agitName(UUID agitUuid) {
		if (agitUuid == null) {
			return null;
		}
		return notificationAgitMemberPersistencePort.findActiveHost(agitUuid)
				.map(NotificationAgitMember::getAgitName)
				.orElse(null);
	}

	private boolean isEnabled(UUID recipientUserUuid, NotificationType type) {
		return userPersistencePort.findByUserUuid(UuidV7.of(recipientUserUuid))
				.flatMap(user -> userNotificationSettingPersistencePort.findByUserId(user.getId()))
				.map(setting -> allows(setting, type))
				.orElse(true);
	}

	private boolean allows(UserNotificationSetting setting, NotificationType type) {
		if (type == NotificationType.POST) {
			return setting.isDiaryNotifyEnabled() || setting.isAgitNotifyEnabled();
		}
		return setting.isAgitNotifyEnabled();
	}
}
