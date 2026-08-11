package com.plip.user.adapter.out.redis;

import com.plip.user.application.port.out.RateLimitPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimitRedisAdapter implements RateLimitPort {

	private static final String KEY_PREFIX = "ratelimit:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public boolean isExceeded(String key, int maxCount, long windowSeconds) {
		String redisKey = KEY_PREFIX + key;
		long now = System.currentTimeMillis();
		long windowStart = now - (windowSeconds * 1_000);

		redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

		Long count = redisTemplate.opsForZSet().zCard(redisKey);
		if (count != null && count >= maxCount) {
			return true;
		}

		redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);
		redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
		return false;
	}
}
