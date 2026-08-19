package com.plip.user.support;

import com.plip.user.application.port.out.RefreshTokenPort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 Redis 대체 Refresh Token 저장소.
 */
public class InMemoryRefreshTokenPort implements RefreshTokenPort {

	private final Map<String, String> store = new ConcurrentHashMap<>();

	@Override
	public void save(String userUuid, String refreshToken, long ttlSeconds) {
		store.put(userUuid, refreshToken);
	}

	@Override
	public String findByUserUuid(String userUuid) {
		return store.get(userUuid);
	}

	@Override
	public void deleteByUserUuid(String userUuid) {
		store.remove(userUuid);
	}
}
