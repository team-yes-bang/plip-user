package com.plip.user.application.service;

import com.plip.user.application.port.in.CompleteSocialSignupCommand;
import com.plip.user.application.port.in.CompleteSocialSignupUseCase;
import com.plip.user.application.port.in.LocalSignupCommand;
import com.plip.user.application.port.in.SignupResult;
import com.plip.user.application.port.in.SocialLoginCommand;
import com.plip.user.application.port.in.SocialLoginUseCase;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompleteSocialSignupService implements CompleteSocialSignupUseCase {

	private final SocialSignupPendingPort socialSignupPendingPort;
	private final SocialLoginUseCase socialLoginUseCase;

	@Override
	public SignupResult complete(CompleteSocialSignupCommand command) {
		SocialSignupPendingPort.SocialSignupPendingRecord pending = consumePending(command.getPendingToken());

		List<LocalSignupCommand.TermAgreementItem> termsAgreements = command.getTermsAgreements() != null
				? command.getTermsAgreements()
				: Collections.emptyList();

		SocialLoginCommand loginCommand = SocialLoginCommand.ofComplete(
				pending.provider(),
				pending.accessToken(),
				termsAgreements
		);

		return socialLoginUseCase.login(loginCommand);
	}

	private SocialSignupPendingPort.SocialSignupPendingRecord consumePending(String pendingToken) {
		if (pendingToken == null || pendingToken.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 가입 정보가 없습니다.");
		}

		SocialSignupPendingPort.SocialSignupPendingRecord record = socialSignupPendingPort
				.findByToken(pendingToken.trim())
				.orElseThrow(() -> new BusinessException(
						ErrorCode.VERIFICATION_TOKEN_INVALID,
						"소셜 가입 정보가 만료되었거나 존재하지 않습니다."
				));

		socialSignupPendingPort.deleteByToken(pendingToken.trim());
		return record;
	}
}
