package com.plip.user.application.port.out;

public interface OtpPort {

	void save(String email, String otpCode, long ttlSeconds);

	String findByEmail(String email);

	void deleteByEmail(String email);
}
