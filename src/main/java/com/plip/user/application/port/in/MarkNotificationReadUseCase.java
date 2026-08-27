package com.plip.user.application.port.in;

public interface MarkNotificationReadUseCase {

	NotificationItemResult markRead(String userUuid, Long notificationId);
}
