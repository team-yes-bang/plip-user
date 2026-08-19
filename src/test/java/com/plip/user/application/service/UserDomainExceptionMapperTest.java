package com.plip.user.application.service;

import com.plip.user.domain.exception.UserDomainError;
import com.plip.user.domain.exception.UserDomainException;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainExceptionMapperTest {

	private final UserDomainExceptionMapper mapper = new UserDomainExceptionMapper();

	@Test
	@DisplayName("유예 만료는 AUTH_011로 매핑")
	void map_grace_expired() {
		BusinessException exception = mapper.toBusinessException(
				new UserDomainException(UserDomainError.GRACE_EXPIRED));

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_WITHDRAWAL_GRACE_EXPIRED);
	}
}
