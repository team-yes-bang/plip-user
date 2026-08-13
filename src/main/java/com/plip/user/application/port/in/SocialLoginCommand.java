package com.plip.user.application.port.in;

import lombok.Getter;

import java.util.List;

@Getter
public class SocialLoginCommand {

	private final String provider;
	private final String accessToken;
	private final List<LocalSignupCommand.TermAgreementItem> termsAgreements;

	private SocialLoginCommand(String provider, String accessToken,
			List<LocalSignupCommand.TermAgreementItem> termsAgreements) {
		this.provider = provider;
		this.accessToken = accessToken;
		this.termsAgreements = termsAgreements;
	}

	public static SocialLoginCommand of(String provider, String accessToken,
			List<LocalSignupCommand.TermAgreementItem> termsAgreements) {
		return new SocialLoginCommand(provider, accessToken, termsAgreements);
	}
}
