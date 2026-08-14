package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class LocalLoginCommand {

	private final String email;
	private final String password;

	private LocalLoginCommand(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public static LocalLoginCommand of(String email, String password) {
		return new LocalLoginCommand(email, password);
	}
}
