package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.TermsAgreementResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 동의 상태")
public class TermsAgreementItemResponse {

	@Schema(description = "약관 ID", example = "3")
	private Long termId;

	@Schema(description = "동의 여부")
	private boolean agreed;

	@Schema(description = "동의 시각")
	private LocalDateTime agreedAt;

	@Schema(description = "철회 시각")
	private LocalDateTime revokedAt;

	private TermsAgreementItemResponse(Long termId, boolean agreed, LocalDateTime agreedAt, LocalDateTime revokedAt) {
		this.termId = termId;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
		this.revokedAt = revokedAt;
	}

	public static TermsAgreementItemResponse from(TermsAgreementResult result) {
		return new TermsAgreementItemResponse(
				result.getTermId(),
				result.isAgreed(),
				result.getAgreedAt(),
				result.getRevokedAt()
		);
	}
}
