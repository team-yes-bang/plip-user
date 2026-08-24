package com.plip.user.application.service;

import com.plip.user.application.port.in.GetUserTermsAgreementsUseCase;
import com.plip.user.application.port.in.UserTermsAgreementItemResult;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserTermsAgreement;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserTermsAgreementsService implements GetUserTermsAgreementsUseCase {

	private final UserPersistencePort userPersistencePort;
	private final TermPersistencePort termPersistencePort;
	private final UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	public List<UserTermsAgreementItemResult> getAgreements(String userUuidValue) {
		UuidV7 userUuid = UuidV7.parse(userUuidValue);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		List<Term> activeTerms = termPersistencePort.findAllActive();
		Map<Long, UserTermsAgreement> agreementByTermId = userTermsAgreementPersistencePort.findAllByUserId(user.getId())
				.stream()
				.collect(Collectors.toMap(UserTermsAgreement::getTermId, agreement -> agreement));

		return activeTerms.stream()
				.map(term -> toResult(term, agreementByTermId.get(term.getId())))
				.toList();
	}

	private UserTermsAgreementItemResult toResult(Term term, UserTermsAgreement agreement) {
		if (agreement == null) {
			return UserTermsAgreementItemResult.of(
					term.getId(),
					term.getTermCode(),
					term.getTitle(),
					term.getContentPath(),
					term.isRequired(),
					false,
					null,
					null
			);
		}

		return UserTermsAgreementItemResult.of(
				term.getId(),
				term.getTermCode(),
				term.getTitle(),
				term.getContentPath(),
				term.isRequired(),
				agreement.isAgreed(),
				agreement.getAgreedAt(),
				agreement.getRevokedAt()
		);
	}
}
