package com.plip.user.application.port.out;

import com.plip.user.domain.model.OtpPurpose;

public interface VerificationTokenPort {

	void save(OtpPurpose purpose, String email, String token, long ttlSeconds);

	String findByEmail(OtpPurpose purpose, String email);

	void deleteByEmail(OtpPurpose purpose, String email);
}
