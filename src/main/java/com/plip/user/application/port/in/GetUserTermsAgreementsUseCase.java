package com.plip.user.application.port.in;

import java.util.List;

public interface GetUserTermsAgreementsUseCase {

	List<UserTermsAgreementItemResult> getAgreements(String userUuid);
}
