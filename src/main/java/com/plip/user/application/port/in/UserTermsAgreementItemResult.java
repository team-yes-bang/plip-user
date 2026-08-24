package com.plip.user.application.port.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTermsAgreementItemResult {

	private Long termId;
	private String termCode;
	private String title;
	private String contentPath;
	private boolean required;
	private boolean agreed;
	private LocalDateTime agreedAt;
	private LocalDateTime revokedAt;

	private UserTermsAgreementItemResult(
			Long termId,
			String termCode,
			String title,
			String contentPath,
			boolean required,
			boolean agreed,
			LocalDateTime agreedAt,
			LocalDateTime revokedAt
	) {
		this.termId = termId;
		this.termCode = termCode;
		this.title = title;
		this.contentPath = contentPath;
		this.required = required;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
		this.revokedAt = revokedAt;
	}

	public static UserTermsAgreementItemResult of(
			Long termId,
			String termCode,
			String title,
			String contentPath,
			boolean required,
			boolean agreed,
			LocalDateTime agreedAt,
			LocalDateTime revokedAt
	) {
		return new UserTermsAgreementItemResult(
				termId, termCode, title, contentPath, required, agreed, agreedAt, revokedAt
		);
	}
}
