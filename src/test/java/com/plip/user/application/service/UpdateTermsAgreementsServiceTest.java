package com.plip.user.application.service;

import com.plip.user.application.port.in.TermsAgreementResult;
import com.plip.user.application.port.in.UpdateTermsAgreementsCommand;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserTermsAgreement;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateTermsAgreementsServiceTest {

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@InjectMocks
	private UpdateTermsAgreementsService updateTermsAgreementsService;

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
	@DisplayName("선택 약관 동의 성공")
	void updateAgreements_optionalAgree() {
		Term marketing = Term.of(3L, null, "마케팅", "terms/marketing", "MARKETING", "1.0",
				false, "ACTIVE", null, null);

		given(termPersistencePort.findAllByIdIn(List.of(3L))).willReturn(List.of(marketing));
		given(userTermsAgreementPersistencePort.findByUserIdAndTermId(1L, 3L)).willReturn(Optional.empty());
		given(userTermsAgreementPersistencePort.save(any(UserTermsAgreement.class)))
				.willAnswer(invocation -> {
					UserTermsAgreement agreement = invocation.getArgument(0);
					return UserTermsAgreement.of(
							10L, agreement.getUserId(), agreement.getTermId(), agreement.isAgreed(),
							agreement.getAgreedAt(), agreement.getRevokedAt(), null, null
					);
				});

		List<TermsAgreementResult> results = updateTermsAgreementsService.updateAgreements(
				UpdateTermsAgreementsCommand.of(USER_UUID.toString(), List.of(
						UpdateTermsAgreementsCommand.TermAgreementItem.of(3L, true)
				))
		);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTermId()).isEqualTo(3L);
		assertThat(results.get(0).isAgreed()).isTrue();
		assertThat(results.get(0).getAgreedAt()).isNotNull();
		verify(userAccountStatusValidator).validateLoginEligible(activeUser);
	}

	@Test
	@DisplayName("선택 약관 철회 성공")
	void updateAgreements_optionalRevoke() {
		Term marketing = Term.of(3L, null, "마케팅", "terms/marketing", "MARKETING", "1.0",
				false, "ACTIVE", null, null);
		UserTermsAgreement existing = UserTermsAgreement.of(
				10L, 1L, 3L, true, LocalDateTime.now().minusDays(1), null, null, null
		);

		given(termPersistencePort.findAllByIdIn(List.of(3L))).willReturn(List.of(marketing));
		given(userTermsAgreementPersistencePort.findByUserIdAndTermId(1L, 3L)).willReturn(Optional.of(existing));
		given(userTermsAgreementPersistencePort.save(any(UserTermsAgreement.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		List<TermsAgreementResult> results = updateTermsAgreementsService.updateAgreements(
				UpdateTermsAgreementsCommand.of(USER_UUID.toString(), List.of(
						UpdateTermsAgreementsCommand.TermAgreementItem.of(3L, false)
				))
		);

		assertThat(results.get(0).isAgreed()).isFalse();
		assertThat(results.get(0).getRevokedAt()).isNotNull();
	}

	@Test
	@DisplayName("필수 약관 철회 시 TERMS_001")
	void updateAgreements_requiredRevokeRejected() {
		Term service = Term.of(1L, null, "서비스", "terms/service", "SERVICE", "1.0",
				true, "ACTIVE", null, null);

		given(termPersistencePort.findAllByIdIn(List.of(1L))).willReturn(List.of(service));

		assertThatThrownBy(() -> updateTermsAgreementsService.updateAgreements(
				UpdateTermsAgreementsCommand.of(USER_UUID.toString(), List.of(
						UpdateTermsAgreementsCommand.TermAgreementItem.of(1L, false)
				))
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.REQUIRED_TERM_REVOKE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("존재하지 않는 약관 ID 시 SIGNUP_004")
	void updateAgreements_termNotFound() {
		given(termPersistencePort.findAllByIdIn(List.of(999L))).willReturn(List.of());

		assertThatThrownBy(() -> updateTermsAgreementsService.updateAgreements(
				UpdateTermsAgreementsCommand.of(USER_UUID.toString(), List.of(
						UpdateTermsAgreementsCommand.TermAgreementItem.of(999L, true)
				))
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.TERM_NOT_FOUND);
	}
}
