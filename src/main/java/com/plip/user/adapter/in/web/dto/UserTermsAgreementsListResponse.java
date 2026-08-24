package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.UserTermsAgreementItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "유저 약관 동의 목록")
public class UserTermsAgreementsListResponse {

	@Schema(description = "ACTIVE 약관별 동의 상태")
	private List<UserTermsAgreementItemResponse> agreements;

	private UserTermsAgreementsListResponse(List<UserTermsAgreementItemResponse> agreements) {
		this.agreements = agreements;
	}

	public static UserTermsAgreementsListResponse of(List<UserTermsAgreementItemResult> results) {
		return new UserTermsAgreementsListResponse(
				results.stream().map(UserTermsAgreementItemResponse::from).toList()
		);
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Schema(description = "유저 약관 동의 항목")
	public static class UserTermsAgreementItemResponse {

		@Schema(description = "약관 ID", example = "3")
		private Long termId;

		@Schema(description = "약관 코드", example = "MARKETING")
		private String termCode;

		@Schema(description = "약관 제목", example = "마케팅 수신 동의")
		private String title;

		@Schema(description = "약관 본문 URL 경로", example = "/terms/marketing")
		private String contentPath;

		@Schema(description = "필수 여부")
		private boolean required;

		@Schema(description = "동의 여부")
		private boolean agreed;

		@Schema(description = "동의 시각")
		private LocalDateTime agreedAt;

		@Schema(description = "철회 시각")
		private LocalDateTime revokedAt;

		private UserTermsAgreementItemResponse(
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

		public static UserTermsAgreementItemResponse from(UserTermsAgreementItemResult result) {
			return new UserTermsAgreementItemResponse(
					result.getTermId(),
					result.getTermCode(),
					result.getTitle(),
					result.getContentPath(),
					result.isRequired(),
					result.isAgreed(),
					result.getAgreedAt(),
					result.getRevokedAt()
			);
		}
	}
}
