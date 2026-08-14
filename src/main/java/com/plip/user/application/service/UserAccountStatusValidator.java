package com.plip.user.application.service;

import com.plip.user.domain.model.User;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class UserAccountStatusValidator {

	private static final String STATUS_ACTIVE = "ACTIVE";

	public void validateLoginEligible(User user) {
		if (user.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.USER_INACTIVE);
		}
		if (!STATUS_ACTIVE.equals(user.getStatus())) {
			throw new BusinessException(ErrorCode.USER_INACTIVE);
		}
	}
}
