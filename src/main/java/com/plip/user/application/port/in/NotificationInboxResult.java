package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationInboxResult {

	private List<NotificationItemResult> items;
	private long unreadCount;

	public static NotificationInboxResult of(List<NotificationItemResult> items, long unreadCount) {
		NotificationInboxResult result = new NotificationInboxResult();
		result.items = List.copyOf(items);
		result.unreadCount = unreadCount;
		return result;
	}
}
