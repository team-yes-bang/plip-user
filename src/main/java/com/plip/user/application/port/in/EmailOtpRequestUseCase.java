package com.plip.user.application.port.in;

public interface EmailOtpRequestUseCase {

	void requestOtp(EmailOtpRequestCommand command);
}
