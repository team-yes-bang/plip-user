package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.UserTermsAgreementEntity;
import com.plip.user.domain.model.UserTermsAgreement;
import org.springframework.stereotype.Component;

@Component
public class UserTermsAgreementEntityMapper {

	public UserTermsAgreement toDomain(UserTermsAgreementEntity entity) {
		return UserTermsAgreement.of(
				entity.getId(),
				entity.getUserId(),
				entity.getTermId(),
				entity.isAgreed(),
				entity.getAgreedAt(),
				entity.getRevokedAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
