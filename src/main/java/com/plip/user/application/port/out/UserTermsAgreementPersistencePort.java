package com.plip.user.application.port.out;

import com.plip.user.domain.model.UserTermsAgreement;

import java.util.List;
import java.util.Optional;

public interface UserTermsAgreementPersistencePort {

	Optional<UserTermsAgreement> findById(Long id);

	List<UserTermsAgreement> saveAll(List<UserTermsAgreement> agreements);
}
