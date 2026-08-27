package com.plip.user.application.port.in;

public interface GetNotificationsUseCase {

	NotificationInboxResult getInbox(String userUuid, int limit);
}
