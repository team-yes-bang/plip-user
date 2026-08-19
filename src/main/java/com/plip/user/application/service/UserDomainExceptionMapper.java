package com.plip.user.application.service;

import com.plip.user.domain.exception.UserDomainError;
import com.plip.user.domain.exception.UserDomainException;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserDomainExceptionMapper {

	public BusinessException toBusinessException(UserDomainException exception) {
		return switch (exception.getError()) {
			case NOT_ACTIVE, ALREADY_WITHDRAWN -> new BusinessException(ErrorCode.USER_INACTIVE);
			case NOT_DELETED -> new BusinessException(ErrorCode.INVALID_INPUT, "복구 대상 계정이 아닙니다.");
			case GRACE_EXPIRED -> new BusinessException(ErrorCode.USER_WITHDRAWAL_GRACE_EXPIRED);
		};
	}
}
