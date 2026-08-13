package com.plip.user.application.port.in;

import lombok.Getter;

import java.util.List;

@Getter
public class LocalSignupCommand {

	private final String email;
	private final String verificationToken;
	private final String password;
	private final String nickname;
	private final List<TermAgreementItem> termsAgreements;

	private LocalSignupCommand(String email, String verificationToken, String password,
			String nickname, List<TermAgreementItem> termsAgreements) {
		this.email = email;
		this.verificationToken = verificationToken;
		this.password = password;
		this.nickname = nickname;
		this.termsAgreements = termsAgreements;
	}

	public static LocalSignupCommand of(String email, String verificationToken, String password,
			String nickname, List<TermAgreementItem> termsAgreements) {
		return new LocalSignupCommand(email, verificationToken, password, nickname, termsAgreements);
	}

	@Getter
	public static class TermAgreementItem {

		private final Long termId;
		private final boolean agreed;

		private TermAgreementItem(Long termId, boolean agreed) {
			this.termId = termId;
			this.agreed = agreed;
		}

		public static TermAgreementItem of(Long termId, boolean agreed) {
			return new TermAgreementItem(termId, agreed);
		}
	}
}
