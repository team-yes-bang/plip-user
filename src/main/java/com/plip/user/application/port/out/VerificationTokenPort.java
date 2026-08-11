package com.plip.user.application.port.out;

public interface VerificationTokenPort {

	void save(String email, String token, long ttlSeconds);

	String findByEmail(String email);

	void deleteByEmail(String email);
}
