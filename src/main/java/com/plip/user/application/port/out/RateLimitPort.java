package com.plip.user.application.port.out;

public interface RateLimitPort {

	/**
	 * @return 제한 초과 시 true
	 */
	boolean isExceeded(String key, int maxCount, long windowSeconds);
}
