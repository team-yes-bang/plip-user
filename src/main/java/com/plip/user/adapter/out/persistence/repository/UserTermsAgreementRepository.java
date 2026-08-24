package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.UserTermsAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreementEntity, Long> {

	Optional<UserTermsAgreementEntity> findByUserIdAndTermId(Long userId, Long termId);

	List<UserTermsAgreementEntity> findAllByUserId(Long userId);
}
