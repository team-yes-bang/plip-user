package com.plip.user.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SocialSignupPendingRedisAdapter implements SocialSignupPendingPort {

	private static final String KEY_PREFIX = "social:signup:pending:";

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void save(String pendingToken, String provider, String accessToken, long ttlSeconds) {
		String payload = serialize(new StoredPayload(provider, accessToken));
		redisTemplate.opsForValue().set(
				buildKey(pendingToken),
				payload,
				ttlSeconds,
				TimeUnit.SECONDS
		);
	}

	@Override
	public Optional<SocialSignupPendingRecord> findByToken(String pendingToken) {
		String raw = redisTemplate.opsForValue().get(buildKey(pendingToken));
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}

		try {
			StoredPayload payload = objectMapper.readValue(raw, StoredPayload.class);
			if (payload.provider == null || payload.accessToken == null) {
				return Optional.empty();
			}
			return Optional.of(new SocialSignupPendingRecord(payload.provider, payload.accessToken));
		} catch (JsonProcessingException exception) {
			return Optional.empty();
		}
	}

	@Override
	public void deleteByToken(String pendingToken) {
		redisTemplate.delete(buildKey(pendingToken));
	}

	private String serialize(StoredPayload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("소셜 가입 pending 직렬화 실패", exception);
		}
	}

	private String buildKey(String pendingToken) {
		return KEY_PREFIX + pendingToken;
	}

	private record StoredPayload(String provider, String accessToken) {
	}
}
