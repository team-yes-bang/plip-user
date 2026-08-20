package com.plip.user.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermVersionComparatorTest {

	@Test
	@DisplayName("major.minor 숫자 세그먼트 기준 비교")
	void compare_numeric_segments() {
		assertThat(TermVersionComparator.compare("3.0", "2.9")).isPositive();
		assertThat(TermVersionComparator.compare("2.0", "1.0")).isPositive();
		assertThat(TermVersionComparator.compare("2.10", "2.9")).isPositive();
		assertThat(TermVersionComparator.compare("1.0", "1.0")).isZero();
	}
}
