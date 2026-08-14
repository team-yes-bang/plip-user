package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.out.RefreshTokenPort;
import com.plip.user.application.port.out.TokenProviderPort;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.config.JwtProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

	private final TokenProviderPort tokenProviderPort;
	private final RefreshTokenPort refreshTokenPort;
	private final JwtProperties jwtProperties;

	public AuthTokenResult issueTokens(UuidV7 userUuid) {
		String accessToken = tokenProviderPort.createAccessToken(userUuid);
		String refreshToken = tokenProviderPort.createRefreshToken(userUuid);
		long refreshTtlSeconds = jwtProperties.getRefreshTokenDays() * 24 * 60 * 60;
		refreshTokenPort.save(userUuid.toString(), refreshToken, refreshTtlSeconds);
		return AuthTokenResult.of(
				accessToken,
				refreshToken,
				jwtProperties.getAccessTokenMinutes() * 60
		);
	}

	public AuthTokenResult reissue(String refreshToken) {
		UuidV7 userUuid = tokenProviderPort.parseRefreshToken(refreshToken);
		String storedToken = refreshTokenPort.findByUserUuid(userUuid.toString());
		if (storedToken == null || !storedToken.equals(refreshToken)) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
		}
		refreshTokenPort.deleteByUserUuid(userUuid.toString());
		return issueTokens(userUuid);
	}

	public void revokeTokens(UuidV7 userUuid) {
		refreshTokenPort.deleteByUserUuid(userUuid.toString());
	}
}
