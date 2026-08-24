package com.plip.user.application.port.out;

import java.util.Optional;

public interface SocialSignupPendingPort {

	void save(String pendingToken, String provider, String accessToken, long ttlSeconds);

	Optional<SocialSignupPendingRecord> findByToken(String pendingToken);

	void deleteByToken(String pendingToken);

	record SocialSignupPendingRecord(String provider, String accessToken) {
	}
}
