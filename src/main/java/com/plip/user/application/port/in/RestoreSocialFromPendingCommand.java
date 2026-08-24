package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class RestoreSocialFromPendingCommand {

	private final String pendingToken;

	private RestoreSocialFromPendingCommand(String pendingToken) {
		this.pendingToken = pendingToken;
	}

	public static RestoreSocialFromPendingCommand of(String pendingToken) {
		return new RestoreSocialFromPendingCommand(pendingToken);
	}
}
