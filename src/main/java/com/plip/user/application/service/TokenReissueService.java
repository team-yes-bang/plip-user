package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.TokenReissueUseCase;
import com.plip.user.application.port.out.TokenProviderPort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenReissueService implements TokenReissueUseCase {

	private final TokenProviderPort tokenProviderPort;
	private final AuthTokenService authTokenService;
	private final UserPersistencePort userPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public AuthTokenResult reissue(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
		}

		UuidV7 userUuid = tokenProviderPort.parseRefreshToken(refreshToken);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

		try {
			userAccountStatusValidator.validateLoginEligible(user);
		} catch (BusinessException e) {
			authTokenService.revokeTokens(userUuid);
			throw e;
		}

		return authTokenService.reissue(refreshToken);
	}
}
