package com.plip.user.adapter.in.web.dto;

import com.plip.user.application.port.in.TermsAgreementResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 동의 변경 응답")
public class TermsAgreementsUpdateResponse {

	@Schema(description = "변경된 약관 동의 목록")
	private List<TermsAgreementItemResponse> agreements;

	private TermsAgreementsUpdateResponse(List<TermsAgreementItemResponse> agreements) {
		this.agreements = agreements;
	}

	public static TermsAgreementsUpdateResponse of(List<TermsAgreementResult> results) {
		return new TermsAgreementsUpdateResponse(
				results.stream().map(TermsAgreementItemResponse::from).toList()
		);
	}
}
