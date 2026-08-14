package com.plip.user.adapter.out.redis;

import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
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
	public void save(OtpPurpose purpose, String email, String token, long ttlSeconds) {
		redisTemplate.opsForValue().set(
				buildKey(purpose, email),
				token,
				ttlSeconds,
				TimeUnit.SECONDS
		);
	}

	@Override
	public String findByEmail(OtpPurpose purpose, String email) {
		return redisTemplate.opsForValue().get(buildKey(purpose, email));
	}

	@Override
	public void deleteByEmail(OtpPurpose purpose, String email) {
		redisTemplate.delete(buildKey(purpose, email));
	}

	private String buildKey(OtpPurpose purpose, String email) {
		return KEY_PREFIX + purpose.name().toLowerCase() + ":email:" + email;
	}
}
