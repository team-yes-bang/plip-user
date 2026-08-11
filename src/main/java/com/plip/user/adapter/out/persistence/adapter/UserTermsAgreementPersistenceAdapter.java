package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.UserTermsAgreementEntityMapper;
import com.plip.user.adapter.out.persistence.repository.UserTermsAgreementRepository;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.domain.model.UserTermsAgreement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserTermsAgreementPersistenceAdapter implements UserTermsAgreementPersistencePort {

	private final UserTermsAgreementRepository userTermsAgreementRepository;
	private final UserTermsAgreementEntityMapper userTermsAgreementEntityMapper;

	@Override
	public Optional<UserTermsAgreement> findById(Long id) {
		return userTermsAgreementRepository.findById(id).map(userTermsAgreementEntityMapper::toDomain);
	}
}
