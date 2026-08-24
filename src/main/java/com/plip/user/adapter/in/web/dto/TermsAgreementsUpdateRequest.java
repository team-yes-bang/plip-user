package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 동의 변경 요청")
public class TermsAgreementsUpdateRequest {

	@NotEmpty(message = "약관 동의 항목은 1개 이상 필요합니다.")
	@Valid
	@Schema(description = "변경할 약관 동의 목록")
	private List<TermAgreementRequest> agreements;
}
