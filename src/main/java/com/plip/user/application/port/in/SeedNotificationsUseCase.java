package com.plip.user.application.port.in;

public interface SeedNotificationsUseCase {

	NotificationInboxResult seed(String userUuid);
}
