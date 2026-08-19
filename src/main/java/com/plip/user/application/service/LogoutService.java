package com.plip.user.application.service;

import com.plip.user.application.port.in.LogoutUseCase;
import com.plip.user.application.port.out.UserLogoutEventPort;
import com.plip.user.domain.model.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogoutService implements LogoutUseCase {

	private final AuthTokenService authTokenService;
	private final UserLogoutEventPort userLogoutEventPort;

	@Override
	@Transactional
	public void logout(String refreshToken) {
		UuidV7 userUuid = authTokenService.validateAndRevokeRefreshToken(refreshToken);
		userLogoutEventPort.publishLogout(userUuid);
	}
}
