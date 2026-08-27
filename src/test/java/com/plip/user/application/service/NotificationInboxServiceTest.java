package com.plip.user.application.service;

import com.plip.user.application.port.in.NotificationInboxResult;
import com.plip.user.application.port.out.UserNotificationPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.NotificationType;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserNotification;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationInboxServiceTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@InjectMocks
	private NotificationInboxService notificationInboxService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private UserNotificationPersistencePort userNotificationPersistencePort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	@BeforeEach
	void setUp() {
		User activeUser = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(activeUser));
	}

	@Test
	@DisplayName("알림함 조회와 미읽음 수를 반환한다")
	void getInbox_success() {
		UserNotification item = UserNotification.of(
				10L,
				USER_UUID.toUuid(),
				NotificationType.CHAT,
				"새 채팅 메시지",
				"안녕",
				"/agit",
				"m1",
				null,
				"chat:m1",
				null,
				LocalDateTime.now(),
				LocalDateTime.now()
		);
		given(userNotificationPersistencePort.findRecentByRecipient(eq(USER_UUID.toUuid()), anyInt()))
				.willReturn(List.of(item));
		given(userNotificationPersistencePort.countUnreadByRecipient(USER_UUID.toUuid())).willReturn(1L);

		NotificationInboxResult result = notificationInboxService.getInbox(USER_UUID.toString(), 30);

		assertThat(result.getItems()).hasSize(1);
		assertThat(result.getItems().get(0).getType()).isEqualTo(NotificationType.CHAT);
		assertThat(result.getUnreadCount()).isEqualTo(1L);
		verify(userAccountStatusValidator).validateLoginEligible(any());
	}

	@Test
	@DisplayName("시드는 중복 키를 건너뛴다")
	void seed_skipsExisting() {
		given(userNotificationPersistencePort.existsByRecipientAndDedupeKey(any(), any())).willReturn(true);
		given(userNotificationPersistencePort.findRecentByRecipient(eq(USER_UUID.toUuid()), anyInt()))
				.willReturn(List.of());
		given(userNotificationPersistencePort.countUnreadByRecipient(USER_UUID.toUuid())).willReturn(0L);

		notificationInboxService.seed(USER_UUID.toString());

		verify(userNotificationPersistencePort, times(0)).save(any());
	}

	@Test
	@DisplayName("시드는 5개 유형을 만든다")
	void seed_createsFiveTypes() {
		given(userNotificationPersistencePort.existsByRecipientAndDedupeKey(any(), any())).willReturn(false);
		given(userNotificationPersistencePort.save(any())).willAnswer(invocation -> invocation.getArgument(0));
		given(userNotificationPersistencePort.findRecentByRecipient(eq(USER_UUID.toUuid()), anyInt()))
				.willReturn(List.of());
		given(userNotificationPersistencePort.countUnreadByRecipient(USER_UUID.toUuid())).willReturn(5L);

		notificationInboxService.seed(USER_UUID.toString());

		ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
		verify(userNotificationPersistencePort, times(5)).save(captor.capture());
		assertThat(captor.getAllValues())
				.extracting(UserNotification::getType)
				.containsExactly(
						NotificationType.CHAT,
						NotificationType.JOIN_REQUEST,
						NotificationType.CREATION,
						NotificationType.TOPIC,
						NotificationType.POST
				);
	}

	@Test
	@DisplayName("없는 알림 읽음은 NOTIFY_003")
	void markRead_notFound() {
		given(userNotificationPersistencePort.findByIdAndRecipient(99L, USER_UUID.toUuid()))
				.willReturn(Optional.empty());

		assertThatThrownBy(() -> notificationInboxService.markRead(USER_UUID.toString(), 99L))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
