package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TermsAgreementResult {

	private Long termId;
	private boolean agreed;
	private LocalDateTime agreedAt;
	private LocalDateTime revokedAt;

	private TermsAgreementResult(Long termId, boolean agreed, LocalDateTime agreedAt, LocalDateTime revokedAt) {
		this.termId = termId;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
		this.revokedAt = revokedAt;
	}

	public static TermsAgreementResult of(Long termId, boolean agreed, LocalDateTime agreedAt, LocalDateTime revokedAt) {
		return new TermsAgreementResult(termId, agreed, agreedAt, revokedAt);
	}
}
