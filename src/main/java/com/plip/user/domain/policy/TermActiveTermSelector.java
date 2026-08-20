package com.plip.user.domain.policy;

import com.plip.user.domain.model.Term;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ACTIVE 약관 중 term_code당 최신 1건만 선택한다.
 * 최신 판단: version(major.minor) → created_at → id.
 */
public final class TermActiveTermSelector {

	private TermActiveTermSelector() {
	}

	public static List<Term> latestPerTermCode(List<Term> activeTerms) {
		Map<String, Term> latestByCode = new LinkedHashMap<>();
		for (Term term : activeTerms) {
			latestByCode.merge(term.getTermCode(), term, TermActiveTermSelector::preferNewer);
		}
		return latestByCode.values().stream()
				.sorted(Comparator.comparing(Term::isRequired).reversed()
						.thenComparing(Term::getTermCode))
				.toList();
	}

	private static Term preferNewer(Term current, Term candidate) {
		int versionCompare = TermVersionComparator.compare(candidate.getVersion(), current.getVersion());
		if (versionCompare > 0) {
			return candidate;
		}
		if (versionCompare < 0) {
			return current;
		}
		int createdAtCompare = compareCreatedAt(candidate, current);
		if (createdAtCompare > 0) {
			return candidate;
		}
		if (createdAtCompare < 0) {
			return current;
		}
		if (candidate.getId() != null && current.getId() != null && candidate.getId() > current.getId()) {
			return candidate;
		}
		return current;
	}

	private static int compareCreatedAt(Term left, Term right) {
		if (left.getCreatedAt() == null && right.getCreatedAt() == null) {
			return 0;
		}
		if (left.getCreatedAt() == null) {
			return -1;
		}
		if (right.getCreatedAt() == null) {
			return 1;
		}
		return left.getCreatedAt().compareTo(right.getCreatedAt());
	}
}
