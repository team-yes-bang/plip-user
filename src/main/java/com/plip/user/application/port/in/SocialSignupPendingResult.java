package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class SocialSignupPendingResult {

	private final String pendingToken;
	private final long expiresInSeconds;

	private SocialSignupPendingResult(String pendingToken, long expiresInSeconds) {
		this.pendingToken = pendingToken;
		this.expiresInSeconds = expiresInSeconds;
	}

	public static SocialSignupPendingResult of(String pendingToken, long expiresInSeconds) {
		return new SocialSignupPendingResult(pendingToken, expiresInSeconds);
	}
}
