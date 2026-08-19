package com.plip.user.application.service;

import com.plip.user.domain.model.User;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class UserAccountStatusValidator {

	public void validateLoginEligible(User user) {
		if (user.isDeleted()) {
			throw new BusinessException(ErrorCode.USER_DELETED_RESTORABLE);
		}
		if (!User.STATUS_ACTIVE.equals(user.getStatus())) {
			throw new BusinessException(ErrorCode.USER_INACTIVE);
		}
	}
}
