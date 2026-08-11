package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class EmailOtpVerifyCommand {

	private final String email;
	private final String otpCode;
	private final String clientIp;

	private EmailOtpVerifyCommand(String email, String otpCode, String clientIp) {
		this.email = email;
		this.otpCode = otpCode;
		this.clientIp = clientIp;
	}

	public static EmailOtpVerifyCommand of(String email, String otpCode, String clientIp) {
		return new EmailOtpVerifyCommand(email, otpCode, clientIp);
	}
}
