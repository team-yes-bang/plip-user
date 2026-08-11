package com.plip.user.adapter.out.redis;

import com.plip.user.application.port.out.VerificationTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class VerificationTokenRedisAdapter implements VerificationTokenPort {

	private static final String KEY_PREFIX = "verification:token:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void save(String email, String token, long ttlSeconds) {
		redisTemplate.opsForValue().set(
				KEY_PREFIX + email,
				token,
				ttlSeconds,
				TimeUnit.SECONDS
		);
	}

	@Override
	public String findByEmail(String email) {
		return redisTemplate.opsForValue().get(KEY_PREFIX + email);
	}

	@Override
	public void deleteByEmail(String email) {
		redisTemplate.delete(KEY_PREFIX + email);
	}
}
