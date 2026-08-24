package com.plip.user.adapter.out.messaging;

import com.plip.user.application.port.out.EventPublisherPort;
import com.plip.user.application.port.out.UserNotificationSettingUpdatedEventPort;
import com.plip.user.domain.model.UuidV7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationSettingUpdatedKafkaAdapter implements UserNotificationSettingUpdatedEventPort {

	private static final String KAFKA_TOPIC = "user.notification-setting-updated";

	private final EventPublisherPort eventPublisherPort;

	@Override
	public void publishSettingUpdated(
			UuidV7 userUuid,
			boolean agitNotifyEnabled,
			boolean diaryNotifyEnabled,
			LocalTime diaryNotifyTime
	) {
		try {
			String payload = String.format(
					"{\"userUuid\":\"%s\",\"agitNotifyEnabled\":%s,\"diaryNotifyEnabled\":%s,"
							+ "\"diaryNotifyTime\":\"%s\",\"occurredAt\":\"%s\"}",
					userUuid.toString(),
					agitNotifyEnabled,
					diaryNotifyEnabled,
					diaryNotifyTime,
					LocalDateTime.now()
			);
			eventPublisherPort.publish(KAFKA_TOPIC, userUuid.toString(), payload);
		} catch (Exception e) {
			log.warn("user.notification-setting-updated 이벤트 발행 실패 (설정 저장은 완료됨): {}", e.getMessage());
		}
	}
}
