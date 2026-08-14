package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class AuthTokenResult {

	private final String accessToken;
	private final String refreshToken;
	private final long accessTokenExpiresIn;

	private AuthTokenResult(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.accessTokenExpiresIn = accessTokenExpiresIn;
	}

	public static AuthTokenResult of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		return new AuthTokenResult(accessToken, refreshToken, accessTokenExpiresIn);
	}
}
