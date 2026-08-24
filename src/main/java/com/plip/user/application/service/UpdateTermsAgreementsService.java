package com.plip.user.application.service;

import com.plip.user.application.port.in.TermsAgreementResult;
import com.plip.user.application.port.in.UpdateTermsAgreementsCommand;
import com.plip.user.application.port.in.UpdateTermsAgreementsUseCase;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateTermsAgreementsService implements UpdateTermsAgreementsUseCase {

	private static final String STATUS_ACTIVE = "ACTIVE";

	private final UserPersistencePort userPersistencePort;
	private final TermPersistencePort termPersistencePort;
	private final UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public List<TermsAgreementResult> updateAgreements(UpdateTermsAgreementsCommand command) {
		if (command.getAgreements() == null || command.getAgreements().isEmpty()) {
			throw new BusinessException(ErrorCode.TERMS_AGREEMENTS_EMPTY);
		}

		UuidV7 userUuid = UuidV7.parse(command.getUserUuid());
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		List<Long> termIds = command.getAgreements().stream()
				.map(UpdateTermsAgreementsCommand.TermAgreementItem::getTermId)
				.toList();

		List<Term> terms = termPersistencePort.findAllByIdIn(termIds);
		Map<Long, Term> termMap = terms.stream()
				.collect(Collectors.toMap(Term::getId, term -> term));

		LocalDateTime now = LocalDateTime.now();
		List<TermsAgreementResult> results = new ArrayList<>();

		for (UpdateTermsAgreementsCommand.TermAgreementItem item : command.getAgreements()) {
			Term term = termMap.get(item.getTermId());
			if (term == null || !STATUS_ACTIVE.equals(term.getStatus())) {
				throw new BusinessException(ErrorCode.TERM_NOT_FOUND);
			}
			if (term.isRequired() && !item.isAgreed()) {
				throw new BusinessException(ErrorCode.REQUIRED_TERM_REVOKE_NOT_ALLOWED);
			}

			UserTermsAgreement agreement = userTermsAgreementPersistencePort
					.findByUserIdAndTermId(user.getId(), item.getTermId())
					.orElseGet(() -> UserTermsAgreement.of(
							null, user.getId(), item.getTermId(), false, null, null, null, null
					));

			if (item.isAgreed()) {
				agreement.agree(now);
			} else {
				agreement.revoke(now);
			}

			UserTermsAgreement saved = userTermsAgreementPersistencePort.save(agreement);
			results.add(TermsAgreementResult.of(
					saved.getTermId(),
					saved.isAgreed(),
					saved.getAgreedAt(),
					saved.getRevokedAt()
			));
		}

		return results;
	}
}
