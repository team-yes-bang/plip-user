package com.plip.user.domain.policy;

import com.plip.user.domain.model.Term;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TermActiveTermSelectorTest {

	@Test
	@DisplayName("동일 term_code ACTIVE 다건이면 version 기준 최신 1건만 반환")
	void latestPerTermCode_prefers_higher_version() {
		LocalDateTime older = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime newer = LocalDateTime.of(2026, 6, 1, 0, 0);

		Term locationV1 = term(29L, "LOCATION", "1.0", false, older);
		Term locationV2 = term(30L, "LOCATION", "2.0", false, newer);
		Term serviceV2 = term(11L, "SERVICE", "2.0", true, newer);

		List<Term> result = TermActiveTermSelector.latestPerTermCode(List.of(locationV1, locationV2, serviceV2));

		assertThat(result).extracting(Term::getId).containsExactly(11L, 30L);
		assertThat(result).extracting(Term::getTermCode).containsExactly("SERVICE", "LOCATION");
	}

	@Test
	@DisplayName("version이 id·created_at보다 우선 — 3.0이 2.9보다 나중 INSERT여도 3.0 선택")
	void latestPerTermCode_version_overrides_insert_order() {
		LocalDateTime firstInserted = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime secondInserted = LocalDateTime.of(2026, 2, 1, 0, 0);

		Term version30First = term(100L, "SERVICE", "3.0", true, firstInserted);
		Term version29Later = term(101L, "SERVICE", "2.9", true, secondInserted);

		List<Term> result = TermActiveTermSelector.latestPerTermCode(List.of(version29Later, version30First));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getVersion()).isEqualTo("3.0");
		assertThat(result.get(0).getId()).isEqualTo(100L);
	}

	private static Term term(Long id, String termCode, String version, boolean required, LocalDateTime createdAt) {
		return Term.of(id, null, termCode + " title", "/terms/" + termCode.toLowerCase(), termCode, version,
				required, "ACTIVE", createdAt, createdAt);
	}
}
