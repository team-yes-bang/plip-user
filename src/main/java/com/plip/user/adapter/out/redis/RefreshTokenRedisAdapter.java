package com.plip.user.adapter.out.redis;

import com.plip.user.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements RefreshTokenPort {

	private static final String KEY_PREFIX = "refresh:token:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void save(String userUuid, String refreshToken, long ttlSeconds) {
		redisTemplate.opsForValue().set(
				KEY_PREFIX + userUuid,
				refreshToken,
				ttlSeconds,
				TimeUnit.SECONDS
		);
	}

	@Override
	public String findByUserUuid(String userUuid) {
		return redisTemplate.opsForValue().get(KEY_PREFIX + userUuid);
	}

	@Override
	public void deleteByUserUuid(String userUuid) {
		redisTemplate.delete(KEY_PREFIX + userUuid);
	}
}
