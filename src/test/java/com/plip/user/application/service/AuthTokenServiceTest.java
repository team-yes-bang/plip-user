package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.out.RefreshTokenPort;
import com.plip.user.application.port.out.TokenProviderPort;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.config.JwtProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

	@InjectMocks
	private AuthTokenService authTokenService;

	@Mock private TokenProviderPort tokenProviderPort;
	@Mock private RefreshTokenPort refreshTokenPort;
	@Mock private JwtProperties jwtProperties;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("토큰 발급 시 Redis에 리프레시 토큰 저장")
	void issueTokens_success() {
		given(tokenProviderPort.createAccessToken(USER_UUID)).willReturn("access-token");
		given(tokenProviderPort.createRefreshToken(USER_UUID)).willReturn("refresh-token");
		given(jwtProperties.getRefreshTokenDays()).willReturn(14L);
		given(jwtProperties.getAccessTokenMinutes()).willReturn(60L);

		AuthTokenResult result = authTokenService.issueTokens(USER_UUID);

		assertThat(result.getAccessToken()).isEqualTo("access-token");
		assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
		assertThat(result.getAccessTokenExpiresIn()).isEqualTo(3600L);
		then(refreshTokenPort).should().save(USER_UUID.toString(), "refresh-token", 14 * 24 * 60 * 60);
	}

	@Test
	@DisplayName("RTR 재발급 시 기존 리프레시 토큰과 불일치하면 실패")
	void reissue_mismatch() {
		given(tokenProviderPort.parseRefreshToken("refresh-token")).willReturn(USER_UUID);
		given(refreshTokenPort.findByUserUuid(USER_UUID.toString())).willReturn("other-token");

		assertThatThrownBy(() -> authTokenService.reissue("refresh-token"))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
	}
}
