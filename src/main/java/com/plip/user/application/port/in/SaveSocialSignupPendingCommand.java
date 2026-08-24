package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class SaveSocialSignupPendingCommand {

	private final String provider;
	private final String accessToken;

	private SaveSocialSignupPendingCommand(String provider, String accessToken) {
		this.provider = provider;
		this.accessToken = accessToken;
	}

	public static SaveSocialSignupPendingCommand of(String provider, String accessToken) {
		return new SaveSocialSignupPendingCommand(provider, accessToken);
	}
}
