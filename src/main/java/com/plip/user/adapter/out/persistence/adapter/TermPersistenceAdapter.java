package com.plip.user.adapter.out.persistence.adapter;

import com.plip.user.adapter.out.persistence.mapper.TermEntityMapper;
import com.plip.user.adapter.out.persistence.repository.TermRepository;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.domain.policy.TermActiveTermSelector;
import com.plip.user.domain.model.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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

	@Override
	public List<Term> findAllActiveRequired() {
		return findAllActive().stream()
				.filter(Term::isRequired)
				.toList();
	}

	@Override
	public List<Term> findAllActive() {
		List<Term> activeTerms = termRepository.findByStatus("ACTIVE").stream()
				.map(termEntityMapper::toDomain)
				.toList();
		return TermActiveTermSelector.latestPerTermCode(activeTerms);
	}

	@Override
	public List<Term> findAllByIdIn(List<Long> ids) {
		return termRepository.findByIdIn(ids).stream()
				.map(termEntityMapper::toDomain)
				.toList();
	}
}
