package com.plip.user.application.port.out;

public interface EmailSendPort {

	void sendOtp(String toEmail, String otpCode);
}
