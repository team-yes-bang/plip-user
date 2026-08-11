package com.plip.user.application.port.out;

import com.plip.user.domain.model.Term;

import java.util.Optional;

public interface TermPersistencePort {

	Optional<Term> findById(Long id);
}
