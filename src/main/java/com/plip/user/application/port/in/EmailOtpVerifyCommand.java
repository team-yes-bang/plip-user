package com.plip.user.application.port.in;

import com.plip.user.domain.model.OtpPurpose;
import lombok.Getter;

@Getter
public class EmailOtpVerifyCommand {

	private final String email;
	private final String otpCode;
	private final String clientIp;
	private final OtpPurpose purpose;

	private EmailOtpVerifyCommand(String email, String otpCode, String clientIp, OtpPurpose purpose) {
		this.email = email;
		this.otpCode = otpCode;
		this.clientIp = clientIp;
		this.purpose = purpose;
	}

	public static EmailOtpVerifyCommand of(String email, String otpCode, String clientIp, OtpPurpose purpose) {
		return new EmailOtpVerifyCommand(email, otpCode, clientIp, purpose);
	}
}
