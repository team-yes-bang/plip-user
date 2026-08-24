package com.plip.user.application.port.in;

import lombok.Getter;

import java.util.List;

@Getter
public class CompleteSocialSignupCommand {

	private final String pendingToken;
	private final List<LocalSignupCommand.TermAgreementItem> termsAgreements;

	private CompleteSocialSignupCommand(
			String pendingToken,
			List<LocalSignupCommand.TermAgreementItem> termsAgreements) {
		this.pendingToken = pendingToken;
		this.termsAgreements = termsAgreements;
	}

	public static CompleteSocialSignupCommand of(
			String pendingToken,
			List<LocalSignupCommand.TermAgreementItem> termsAgreements) {
		return new CompleteSocialSignupCommand(pendingToken, termsAgreements);
	}
}
