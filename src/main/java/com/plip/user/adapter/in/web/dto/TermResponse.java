package com.plip.user.adapter.in.web.dto;

import com.plip.user.domain.model.Term;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 항목")
public class TermResponse {

	@Schema(description = "약관 ID", example = "1")
	private Long id;

	@Schema(description = "약관 제목", example = "서비스 이용약관")
	private String title;

	@Schema(description = "약관 본문 URL 경로", example = "/terms/service")
	private String contentPath;

	@Schema(description = "약관 코드", example = "SERVICE")
	private String termCode;

	@Schema(description = "약관 버전", example = "v1.0")
	private String version;

	@Schema(description = "필수 여부")
	private boolean required;

	private TermResponse(Long id, String title, String contentPath, String termCode, String version, boolean required) {
		this.id = id;
		this.title = title;
		this.contentPath = contentPath;
		this.termCode = termCode;
		this.version = version;
		this.required = required;
	}

	public static TermResponse from(Term term) {
		return new TermResponse(
				term.getId(),
				term.getTitle(),
				term.getContentPath(),
				term.getTermCode(),
				term.getVersion(),
				term.isRequired()
		);
	}
}
