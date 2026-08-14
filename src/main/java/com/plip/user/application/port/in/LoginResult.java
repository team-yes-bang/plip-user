package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class LoginResult {

	private final String userUuid;
	private final AuthTokenResult tokens;

	private LoginResult(String userUuid, AuthTokenResult tokens) {
		this.userUuid = userUuid;
		this.tokens = tokens;
	}

	public static LoginResult of(String userUuid, AuthTokenResult tokens) {
		return new LoginResult(userUuid, tokens);
	}
}
