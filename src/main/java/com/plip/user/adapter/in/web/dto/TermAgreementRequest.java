package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 동의 항목")
public class TermAgreementRequest {

	@NotNull(message = "약관 ID는 필수입니다.")
	@Schema(description = "약관 ID", example = "1")
	private Long termId;

	@NotNull(message = "동의 여부는 필수입니다.")
	@Schema(description = "동의 여부", example = "true")
	private Boolean agreed;
}
