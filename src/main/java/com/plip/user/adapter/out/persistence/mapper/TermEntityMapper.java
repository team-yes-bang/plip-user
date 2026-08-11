package com.plip.user.adapter.out.persistence.mapper;

import com.plip.user.adapter.out.persistence.entity.TermEntity;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.UuidV7;
import org.springframework.stereotype.Component;

@Component
public class TermEntityMapper {

	public Term toDomain(TermEntity entity) {
		return Term.of(
				entity.getId(),
				UuidV7.of(entity.getTermUuid()),
				entity.getTitle(),
				entity.getContentPath(),
				entity.getTermCode(),
				entity.getVersion(),
				entity.isRequired(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
