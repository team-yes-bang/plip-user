package com.plip.user.application.port.in;

public interface GetUnreadNotificationCountUseCase {

	long getUnreadCount(String userUuid);
}
