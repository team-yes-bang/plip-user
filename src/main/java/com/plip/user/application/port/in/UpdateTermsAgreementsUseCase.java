package com.plip.user.application.port.in;

import java.util.List;

public interface UpdateTermsAgreementsUseCase {

	List<TermsAgreementResult> updateAgreements(UpdateTermsAgreementsCommand command);
}
