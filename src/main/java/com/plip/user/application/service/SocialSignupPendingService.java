package com.plip.user.application.service;

import com.plip.user.application.port.in.SaveSocialSignupPendingCommand;
import com.plip.user.application.port.in.SaveSocialSignupPendingUseCase;
import com.plip.user.application.port.in.SocialSignupPendingResult;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.config.OtpProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialSignupPendingService implements SaveSocialSignupPendingUseCase {

	private final SocialSignupPendingPort socialSignupPendingPort;
	private final OtpProperties otpProperties;

	@Override
	public SocialSignupPendingResult savePending(SaveSocialSignupPendingCommand command) {
		if (command.getProvider() == null || command.getProvider().isBlank()
				|| command.getAccessToken() == null || command.getAccessToken().isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		String pendingToken = UUID.randomUUID().toString();
		long expiresInSeconds = otpProperties.getVerificationTokenTtlSeconds();
		socialSignupPendingPort.save(
				pendingToken,
				command.getProvider().trim().toLowerCase(),
				command.getAccessToken(),
				expiresInSeconds
		);
		return SocialSignupPendingResult.of(pendingToken, expiresInSeconds);
	}
}
