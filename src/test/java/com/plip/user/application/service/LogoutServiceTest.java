package com.plip.user.application.service;

import com.plip.user.application.port.out.UserLogoutEventPort;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

	@InjectMocks
	private LogoutService logoutService;

	@Mock private AuthTokenService authTokenService;
	@Mock private UserLogoutEventPort userLogoutEventPort;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final String REFRESH_TOKEN = "refresh-token";

	@Test
	@DisplayName("로그아웃 성공 시 Refresh 파기 및 Kafka 이벤트 발행")
	void logout_success() {
		given(authTokenService.validateAndRevokeRefreshToken(REFRESH_TOKEN)).willReturn(USER_UUID);

		logoutService.logout(REFRESH_TOKEN);

		then(userLogoutEventPort).should().publishLogout(USER_UUID);
	}

	@Test
	@DisplayName("리프레시 토큰 무효 시 실패")
	void logout_invalid_refresh_token() {
		given(authTokenService.validateAndRevokeRefreshToken(REFRESH_TOKEN))
				.willThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

		assertThatThrownBy(() -> logoutService.logout(REFRESH_TOKEN))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
	}
}
