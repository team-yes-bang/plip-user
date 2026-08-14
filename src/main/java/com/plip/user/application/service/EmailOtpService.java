package com.plip.user.application.service;

import com.plip.user.application.port.in.EmailOtpRequestCommand;
import com.plip.user.application.port.in.EmailOtpRequestUseCase;
import com.plip.user.application.port.in.EmailOtpVerifyCommand;
import com.plip.user.application.port.in.EmailOtpVerifyResult;
import com.plip.user.application.port.in.EmailOtpVerifyUseCase;
import com.plip.user.application.port.out.EmailSendPort;
import com.plip.user.application.port.out.OtpPort;
import com.plip.user.application.port.out.RateLimitPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.global.config.OtpProperties;
import com.plip.user.global.config.RateLimitProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailOtpService implements EmailOtpRequestUseCase, EmailOtpVerifyUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final OtpPort otpPort;
	private final VerificationTokenPort verificationTokenPort;
	private final RateLimitPort rateLimitPort;
	private final EmailSendPort emailSendPort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final OtpProperties otpProperties;
	private final RateLimitProperties rateLimitProperties;

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long MINUTE_SECONDS = 60L;
	private static final long DAY_SECONDS = 86_400L;

	@Override
	public void requestOtp(EmailOtpRequestCommand command) {
		OtpPurpose purpose = command.getPurpose();
		checkRateLimit(command.getEmail(), command.getClientIp(), purpose);

		if (purpose == OtpPurpose.SIGNUP) {
			ensureLocalEmailNotRegistered(command.getEmail());
		} else if (purpose == OtpPurpose.PASSWORD_RESET) {
			if (!hasActiveLocalAccount(command.getEmail())) {
				return;
			}
		}

		String otpCode = generateOtp(otpProperties.getLength());
		otpPort.save(purpose, command.getEmail(), otpCode, otpProperties.getTtlSeconds());
		emailSendPort.sendOtp(command.getEmail(), otpCode);
	}

	private void ensureLocalEmailNotRegistered(String email) {
		if (userAuthPersistencePort.findByEmailAndAuthType(email, AUTH_TYPE_LOCAL).isPresent()) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
		}
	}

	private boolean hasActiveLocalAccount(String email) {
		return userAuthPersistencePort.findByEmailAndAuthType(email, AUTH_TYPE_LOCAL).isPresent();
	}

	@Override
	public EmailOtpVerifyResult verifyOtp(EmailOtpVerifyCommand command) {
		OtpPurpose purpose = command.getPurpose();
		String storedOtp = otpPort.findByEmail(purpose, command.getEmail());

		if (storedOtp == null) {
			throw new BusinessException(ErrorCode.OTP_EXPIRED_OR_NOT_FOUND);
		}
		if (!storedOtp.equals(command.getOtpCode())) {
			throw new BusinessException(ErrorCode.OTP_MISMATCH);
		}

		otpPort.deleteByEmail(purpose, command.getEmail());

		String token = UUID.randomUUID().toString();
		verificationTokenPort.save(
				purpose,
				command.getEmail(),
				token,
				otpProperties.getVerificationTokenTtlSeconds()
		);

		return EmailOtpVerifyResult.of(token);
	}

	private void checkRateLimit(String email, String clientIp, OtpPurpose purpose) {
		RateLimitProperties.Limit emailLimit = rateLimitProperties.resolveEmailLimit(purpose);
		RateLimitProperties.Limit ipLimit = rateLimitProperties.resolveIpLimit(purpose);
		String purposeKey = purpose.name().toLowerCase();

		if (rateLimitPort.isExceeded(
				"otp:" + purposeKey + ":email:minute:" + email, emailLimit.getMinuteMax(), MINUTE_SECONDS)) {
			throw new BusinessException(ErrorCode.OTP_RATE_LIMIT_EXCEEDED);
		}
		if (rateLimitPort.isExceeded(
				"otp:" + purposeKey + ":email:daily:" + email, emailLimit.getDailyMax(), DAY_SECONDS)) {
			throw new BusinessException(ErrorCode.OTP_RATE_LIMIT_EXCEEDED);
		}
		if (rateLimitPort.isExceeded(
				"otp:" + purposeKey + ":ip:minute:" + clientIp, ipLimit.getMinuteMax(), MINUTE_SECONDS)) {
			throw new BusinessException(ErrorCode.OTP_RATE_LIMIT_EXCEEDED);
		}
		if (rateLimitPort.isExceeded(
				"otp:" + purposeKey + ":ip:daily:" + clientIp, ipLimit.getDailyMax(), DAY_SECONDS)) {
			throw new BusinessException(ErrorCode.OTP_RATE_LIMIT_EXCEEDED);
		}
	}

	private String generateOtp(int length) {
		int bound = (int) Math.pow(10, length);
		int otp = RANDOM.nextInt(bound);
		return String.format("%0" + length + "d", otp);
	}
}
