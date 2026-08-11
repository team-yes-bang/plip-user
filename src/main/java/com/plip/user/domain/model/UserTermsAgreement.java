package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreement {

	private Long id;
	private Long userId;
	private Long termId;
	private boolean agreed;
	private LocalDateTime agreedAt;
	private LocalDateTime revokedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static UserTermsAgreement of(
			Long id,
			Long userId,
			Long termId,
			boolean agreed,
			LocalDateTime agreedAt,
			LocalDateTime revokedAt,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		UserTermsAgreement agreement = new UserTermsAgreement();
		agreement.id = id;
		agreement.userId = userId;
		agreement.termId = termId;
		agreement.agreed = agreed;
		agreement.agreedAt = agreedAt;
		agreement.revokedAt = revokedAt;
		agreement.createdAt = createdAt;
		agreement.updatedAt = updatedAt;
		return agreement;
	}
}
