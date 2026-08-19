package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestoreLocalCommand {

	private String email;
	private String password;

	private RestoreLocalCommand(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public static RestoreLocalCommand of(String email, String password) {
		return new RestoreLocalCommand(email, password);
	}
}
