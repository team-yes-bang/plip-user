package com.plip.user.application.port.out;

import com.plip.user.domain.model.Term;

import java.util.List;
import java.util.Optional;

public interface TermPersistencePort {

	Optional<Term> findById(Long id);

	List<Term> findAllActiveRequired();

	List<Term> findAllByIdIn(List<Long> ids);
}
