package com.plip.user.adapter.in.web.dto;

import com.plip.user.domain.model.Term;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "ACTIVE 약관 목록 응답")
public class TermsListResponse {

	@Schema(description = "ACTIVE 약관 목록")
	private List<TermResponse> terms;

	private TermsListResponse(List<TermResponse> terms) {
		this.terms = terms;
	}

	public static TermsListResponse of(List<Term> terms) {
		return new TermsListResponse(terms.stream().map(TermResponse::from).toList());
	}
}
