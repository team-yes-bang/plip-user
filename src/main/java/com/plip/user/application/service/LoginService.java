package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LocalLoginCommand;
import com.plip.user.application.port.in.LocalLoginUseCase;
import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService implements LocalLoginUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserPersistencePort userPersistencePort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final AuthTokenService authTokenService;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public LoginResult login(LocalLoginCommand command) {
		String email = command.getEmail() == null ? "" : command.getEmail().trim();
		String password = command.getPassword();

		if (email.isBlank() || password == null || password.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		UserAuth userAuth = userAuthPersistencePort.findByEmailAndAuthType(email, AUTH_TYPE_LOCAL)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoderPort.matches(password, userAuth.getPasswordHash())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		User user = userPersistencePort.findById(userAuth.getUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		userAccountStatusValidator.validateLoginEligible(user);

		AuthTokenResult tokens = authTokenService.issueTokens(user.getUserUuid());
		return LoginResult.of(user.getUserUuid().toString(), tokens);
	}
}
