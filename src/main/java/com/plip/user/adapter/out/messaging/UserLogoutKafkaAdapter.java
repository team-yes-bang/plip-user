package com.plip.user.adapter.out.messaging;

import com.plip.user.application.port.out.EventPublisherPort;
import com.plip.user.application.port.out.UserLogoutEventPort;
import com.plip.user.domain.model.UuidV7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLogoutKafkaAdapter implements UserLogoutEventPort {

	private static final String KAFKA_TOPIC_USER_LOGOUT = "user.logout";

	private final EventPublisherPort eventPublisherPort;

	@Override
	public void publishLogout(UuidV7 userUuid) {
		try {
			String payload = String.format(
					"{\"userUuid\":\"%s\",\"occurredAt\":\"%s\"}",
					userUuid.toString(),
					LocalDateTime.now()
			);
			eventPublisherPort.publish(KAFKA_TOPIC_USER_LOGOUT, userUuid.toString(), payload);
		} catch (Exception e) {
			log.warn("user.logout 이벤트 발행 실패 (로그아웃은 완료됨): {}", e.getMessage());
		}
	}
}
