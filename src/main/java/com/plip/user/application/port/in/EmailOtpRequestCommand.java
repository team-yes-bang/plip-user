package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class EmailOtpRequestCommand {

	private final String email;
	private final String clientIp;

	private EmailOtpRequestCommand(String email, String clientIp) {
		this.email = email;
		this.clientIp = clientIp;
	}

	public static EmailOtpRequestCommand of(String email, String clientIp) {
		return new EmailOtpRequestCommand(email, clientIp);
	}
}
