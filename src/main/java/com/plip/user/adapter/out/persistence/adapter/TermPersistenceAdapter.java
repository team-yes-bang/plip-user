package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.TermEntityMapper;
import com.plip.user.adapter.out.persistence.repository.TermRepository;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.domain.model.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TermPersistenceAdapter implements TermPersistencePort {

	private final TermRepository termRepository;
	private final TermEntityMapper termEntityMapper;

	@Override
	public Optional<Term> findById(Long id) {
		return termRepository.findById(id).map(termEntityMapper::toDomain);
	}
}
