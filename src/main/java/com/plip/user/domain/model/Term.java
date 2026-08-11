package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

	private Long id;
	private UuidV7 termUuid;
	private String title;
	private String contentPath;
	private String termCode;
	private String version;
	private boolean required;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static Term of(
			Long id,
			UuidV7 termUuid,
			String title,
			String contentPath,
			String termCode,
			String version,
			boolean required,
			String status,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		Term term = new Term();
		term.id = id;
		term.termUuid = termUuid;
		term.title = title;
		term.contentPath = contentPath;
		term.termCode = termCode;
		term.version = version;
		term.required = required;
		term.status = status;
		term.createdAt = createdAt;
		term.updatedAt = updatedAt;
		return term;
	}
}
