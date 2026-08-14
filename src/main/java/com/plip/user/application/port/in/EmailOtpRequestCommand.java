package com.plip.user.application.port.in;

import com.plip.user.domain.model.OtpPurpose;
import lombok.Getter;

@Getter
public class EmailOtpRequestCommand {

	private final String email;
	private final String clientIp;
	private final OtpPurpose purpose;

	private EmailOtpRequestCommand(String email, String clientIp, OtpPurpose purpose) {
		this.email = email;
		this.clientIp = clientIp;
		this.purpose = purpose;
	}

	public static EmailOtpRequestCommand of(String email, String clientIp, OtpPurpose purpose) {
		return new EmailOtpRequestCommand(email, clientIp, purpose);
	}
}
