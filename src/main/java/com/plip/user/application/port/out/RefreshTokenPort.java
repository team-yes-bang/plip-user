package com.plip.user.application.port.out;

public interface RefreshTokenPort {

	void save(String userUuid, String refreshToken, long ttlSeconds);

	String findByUserUuid(String userUuid);

	void deleteByUserUuid(String userUuid);
}
