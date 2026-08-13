package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class SignupResult {

	private final String userUuid;
	private final boolean newUser;

	private SignupResult(String userUuid, boolean newUser) {
		this.userUuid = userUuid;
		this.newUser = newUser;
	}

	public static SignupResult of(String userUuid) {
		return new SignupResult(userUuid, true);
	}

	public static SignupResult of(String userUuid, boolean newUser) {
		return new SignupResult(userUuid, newUser);
	}
}
