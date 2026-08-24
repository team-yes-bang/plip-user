package com.plip.user.application.service;

import com.plip.user.application.port.in.UserTermsAgreementItemResult;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserTermsAgreement;
import com.plip.user.domain.model.UuidV7;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetUserTermsAgreementsServiceTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@InjectMocks
	private GetUserTermsAgreementsService getUserTermsAgreementsService;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Mock
	private TermPersistencePort termPersistencePort;

	@Mock
	private UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;

	@Mock
	private UserAccountStatusValidator userAccountStatusValidator;

	private User activeUser;

	@BeforeEach
	void setUp() {
		activeUser = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(activeUser));
	}

	@Test
	@DisplayName("ACTIVE 약관과 유저 동의 상태 merge 조회")
	void getAgreements_mergeActiveTermsAndUserState() {
		Term service = Term.of(1L, null, "서비스", "terms/service", "SERVICE", "1.0",
				true, "ACTIVE", null, null);
		Term marketing = Term.of(3L, null, "마케팅", "terms/marketing", "MARKETING", "1.0",
				false, "ACTIVE", null, null);
		UserTermsAgreement serviceAgreement = UserTermsAgreement.of(
				10L, 1L, 1L, true, LocalDateTime.now().minusDays(1), null, null, null
		);

		given(termPersistencePort.findAllActive()).willReturn(List.of(service, marketing));
		given(userTermsAgreementPersistencePort.findAllByUserId(1L)).willReturn(List.of(serviceAgreement));

		List<UserTermsAgreementItemResult> results = getUserTermsAgreementsService.getAgreements(
				USER_UUID.toString()
		);

		assertThat(results).hasSize(2);
		assertThat(results.get(0).getTermCode()).isEqualTo("SERVICE");
		assertThat(results.get(0).isAgreed()).isTrue();
		assertThat(results.get(1).getTermCode()).isEqualTo("MARKETING");
		assertThat(results.get(1).isAgreed()).isFalse();
		verify(userAccountStatusValidator).validateLoginEligible(activeUser);
	}
}
