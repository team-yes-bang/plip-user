package com.plip.user.adapter.out.messaging;

import com.plip.user.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaEventPublisherAdapter implements EventPublisherPort {

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Override
	public void publish(String topic, String key, String payload) {
		kafkaTemplate.send(topic, key, payload);
	}
}
