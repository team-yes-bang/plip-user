package com.plip.user.application.port.in;

import lombok.Getter;

@Getter
public class EmailOtpVerifyResult {

	private final String verificationToken;

	private EmailOtpVerifyResult(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public static EmailOtpVerifyResult of(String verificationToken) {
		return new EmailOtpVerifyResult(verificationToken);
	}
}
