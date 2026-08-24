package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserTermsAgreement;

import java.util.List;
import java.util.Optional;

public interface UserTermsAgreementPersistencePort {

	Optional<UserTermsAgreement> findById(Long id);

	Optional<UserTermsAgreement> findByUserIdAndTermId(Long userId, Long termId);

	List<UserTermsAgreement> findAllByUserId(Long userId);

	UserTermsAgreement save(UserTermsAgreement agreement);

	List<UserTermsAgreement> saveAll(List<UserTermsAgreement> agreements);
}
