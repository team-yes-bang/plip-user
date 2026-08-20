package com.plip.user.application.port.in;

import com.plip.user.domain.model.Term;

import java.util.List;

public interface GetActiveTermsUseCase {

	List<Term> getActiveTerms();
}
