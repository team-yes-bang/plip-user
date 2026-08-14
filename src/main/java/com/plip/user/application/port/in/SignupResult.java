package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class SignupResult {

	private final String userUuid;
	private final boolean newUser;
	private final AuthTokenResult tokens;

	private SignupResult(String userUuid, boolean newUser, AuthTokenResult tokens) {
		this.userUuid = userUuid;
		this.newUser = newUser;
		this.tokens = tokens;
	}

	public static SignupResult of(String userUuid, AuthTokenResult tokens) {
		return new SignupResult(userUuid, true, tokens);
	}

	public static SignupResult of(String userUuid, boolean newUser, AuthTokenResult tokens) {
		return new SignupResult(userUuid, newUser, tokens);
	}
}
