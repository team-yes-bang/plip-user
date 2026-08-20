package com.plip.user.application.service;

import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.domain.model.Term;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TermQueryServiceTest {

	@Mock private TermPersistencePort termPersistencePort;
	@InjectMocks private TermQueryService termQueryService;

	@Test
	@DisplayName("ACTIVE 약관 목록 조회")
	void getActiveTerms() {
		Term serviceTerm = Term.of(
				1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "1.0", true, "ACTIVE", null, null);
		given(termPersistencePort.findAllActive()).willReturn(List.of(serviceTerm));

		assertThat(termQueryService.getActiveTerms()).containsExactly(serviceTerm);
	}
}
