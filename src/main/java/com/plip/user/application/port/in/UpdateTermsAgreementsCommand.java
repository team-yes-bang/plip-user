package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTermsAgreementsCommand {

	private String userUuid;
	private List<TermAgreementItem> agreements;

	private UpdateTermsAgreementsCommand(String userUuid, List<TermAgreementItem> agreements) {
		this.userUuid = userUuid;
		this.agreements = agreements;
	}

	public static UpdateTermsAgreementsCommand of(String userUuid, List<TermAgreementItem> agreements) {
		return new UpdateTermsAgreementsCommand(userUuid, agreements);
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class TermAgreementItem {

		private Long termId;
		private boolean agreed;

		private TermAgreementItem(Long termId, boolean agreed) {
			this.termId = termId;
			this.agreed = agreed;
		}

		public static TermAgreementItem of(Long termId, boolean agreed) {
			return new TermAgreementItem(termId, agreed);
		}
	}
}
