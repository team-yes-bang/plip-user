package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class PasswordResetCommand {

	private final String email;
	private final String verificationToken;
	private final String newPassword;

	private PasswordResetCommand(String email, String verificationToken, String newPassword) {
		this.email = email;
		this.verificationToken = verificationToken;
		this.newPassword = newPassword;
	}

	public static PasswordResetCommand of(String email, String verificationToken, String newPassword) {
		return new PasswordResetCommand(email, verificationToken, newPassword);
	}
}
