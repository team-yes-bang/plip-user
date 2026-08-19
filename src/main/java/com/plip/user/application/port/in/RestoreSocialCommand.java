package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestoreSocialCommand {

	private String provider;
	private String accessToken;

	private RestoreSocialCommand(String provider, String accessToken) {
		this.provider = provider;
		this.accessToken = accessToken;
	}

	public static RestoreSocialCommand of(String provider, String accessToken) {
		return new RestoreSocialCommand(provider, accessToken);
	}
}
