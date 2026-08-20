package com.plip.user.application.service;

import com.plip.user.application.port.in.GetActiveTermsUseCase;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.domain.model.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermQueryService implements GetActiveTermsUseCase {

	private final TermPersistencePort termPersistencePort;

	@Override
	@Transactional(readOnly = true)
	public List<Term> getActiveTerms() {
		return termPersistencePort.findAllActive();
	}
}
