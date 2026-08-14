package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class PasswordChangeCommand {

	private final String userUuid;
	private final String currentPassword;
	private final String newPassword;

	private PasswordChangeCommand(String userUuid, String currentPassword, String newPassword) {
		this.userUuid = userUuid;
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
	}

	public static PasswordChangeCommand of(String userUuid, String currentPassword, String newPassword) {
		return new PasswordChangeCommand(userUuid, currentPassword, newPassword);
	}
}
