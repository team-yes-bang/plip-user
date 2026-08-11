package com.plip.user.application.port.in;

public interface EmailOtpVerifyUseCase {

	EmailOtpVerifyResult verifyOtp(EmailOtpVerifyCommand command);
}
