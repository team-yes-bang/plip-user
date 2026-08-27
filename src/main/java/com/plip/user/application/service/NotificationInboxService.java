package com.plip.user.application.service;

import com.plip.user.application.port.in.GetNotificationsUseCase;
import com.plip.user.application.port.in.GetUnreadNotificationCountUseCase;
import com.plip.user.application.port.in.MarkAllNotificationsReadUseCase;
import com.plip.user.application.port.in.MarkNotificationReadUseCase;
import com.plip.user.application.port.in.NotificationInboxResult;
import com.plip.user.application.port.in.NotificationItemResult;
import com.plip.user.application.port.in.SeedNotificationsUseCase;
import com.plip.user.application.port.out.UserNotificationPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.NotificationType;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserNotification;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationInboxService implements GetNotificationsUseCase, GetUnreadNotificationCountUseCase,
		MarkNotificationReadUseCase, MarkAllNotificationsReadUseCase, SeedNotificationsUseCase {

	private static final int DEFAULT_LIMIT = 30;
	private static final int MAX_LIMIT = 50;

	private final UserPersistencePort userPersistencePort;
	private final UserNotificationPersistencePort userNotificationPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional(readOnly = true)
	public NotificationInboxResult getInbox(String userUuidValue, int limit) {
		UUID recipient = requireEligibleUser(userUuidValue).getUserUuid().toUuid();
		int pageSize = normalizeLimit(limit);
		List<NotificationItemResult> items = userNotificationPersistencePort
				.findRecentByRecipient(recipient, pageSize)
				.stream()
				.map(this::toResult)
				.toList();
		return NotificationInboxResult.of(items, userNotificationPersistencePort.countUnreadByRecipient(recipient));
	}

	@Override
	@Transactional(readOnly = true)
	public long getUnreadCount(String userUuidValue) {
		UUID recipient = requireEligibleUser(userUuidValue).getUserUuid().toUuid();
		return userNotificationPersistencePort.countUnreadByRecipient(recipient);
	}

	@Override
	@Transactional
	public NotificationItemResult markRead(String userUuidValue, Long notificationId) {
		UUID recipient = requireEligibleUser(userUuidValue).getUserUuid().toUuid();
		UserNotification notification = userNotificationPersistencePort
				.findByIdAndRecipient(notificationId, recipient)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
		notification.markRead(LocalDateTime.now());
		return toResult(userNotificationPersistencePort.save(notification));
	}

	@Override
	@Transactional
	public long markAllRead(String userUuidValue) {
		UUID recipient = requireEligibleUser(userUuidValue).getUserUuid().toUuid();
		return userNotificationPersistencePort.markAllRead(recipient);
	}

	@Override
	@Transactional
	public NotificationInboxResult seed(String userUuidValue) {
		UUID recipient = requireEligibleUser(userUuidValue).getUserUuid().toUuid();
		for (SeedItem item : seedItems()) {
			if (userNotificationPersistencePort.existsByRecipientAndDedupeKey(recipient, item.dedupeKey())) {
				continue;
			}
			userNotificationPersistencePort.save(UserNotification.create(
					recipient,
					item.type(),
					item.title(),
					item.body(),
					item.deepLink(),
					item.resourceId(),
					null,
					item.dedupeKey()
			));
		}
		return getInbox(userUuidValue, DEFAULT_LIMIT);
	}

	private User requireEligibleUser(String userUuidValue) {
		UuidV7 userUuid = UuidV7.parse(userUuidValue);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		userAccountStatusValidator.validateLoginEligible(user);
		return user;
	}

	private NotificationItemResult toResult(UserNotification notification) {
		return NotificationItemResult.of(
				notification.getId(),
				notification.getType(),
				notification.getTitle(),
				notification.getBody(),
				notification.getDeepLink(),
				notification.getResourceId(),
				notification.getAgitUuid(),
				notification.isRead(),
				notification.getCreatedAt()
		);
	}

	private int normalizeLimit(int limit) {
		if (limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	private List<SeedItem> seedItems() {
		return List.of(
				new SeedItem(
						NotificationType.CHAT,
						"새 채팅 메시지",
						"아지트에서 새 메시지가 도착했습니다.",
						"/agit",
						"seed-chat",
						"seed:chat"
				),
				new SeedItem(
						NotificationType.JOIN_REQUEST,
						"입장 요청",
						"누군가 아지트 입장을 요청했습니다.",
						"/agit",
						"seed-join",
						"seed:join-request"
				),
				new SeedItem(
						NotificationType.CREATION,
						"아지트가 생성되었습니다",
						"새 아지트가 준비되었습니다.",
						"/agit",
						"seed-creation",
						"seed:creation"
				),
				new SeedItem(
						NotificationType.TOPIC,
						"새 토픽",
						"아지트에 새 토픽이 열렸습니다.",
						"/agit",
						"seed-topic",
						"seed:topic"
				),
				new SeedItem(
						NotificationType.POST,
						"새로운 글",
						"다이어리·피드에 새 글이 올라왔습니다.",
						"/diary",
						"seed-post",
						"seed:post"
				)
		);
	}

	private record SeedItem(
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			String dedupeKey
	) {
	}
}
