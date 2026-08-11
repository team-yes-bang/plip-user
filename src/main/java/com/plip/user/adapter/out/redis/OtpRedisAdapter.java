package com.plip.user.adapter.out.redis;

import com.plip.user.application.port.out.OtpPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OtpRedisAdapter implements OtpPort {

	private static final String KEY_PREFIX = "otp:email:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void save(String email, String otpCode, long ttlSeconds) {
		redisTemplate.opsForValue().set(
				KEY_PREFIX + email,
				otpCode,
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
