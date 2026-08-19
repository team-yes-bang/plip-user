package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.in.RestoreLocalCommand;
import com.plip.user.application.port.in.RestoreSocialCommand;
import com.plip.user.application.port.in.RestoreUserUseCase;
import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.exception.UserDomainException;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestoreUserService implements RestoreUserUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserPersistencePort userPersistencePort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final OAuthUserInfoPort oAuthUserInfoPort;
	private final AuthTokenService authTokenService;
	private final UserDomainExceptionMapper userDomainExceptionMapper;

	@Override
	@Transactional
	public LoginResult restoreLocal(RestoreLocalCommand command) {
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

		return restoreAndIssueTokens(user);
	}

	@Override
	@Transactional
	public LoginResult restoreSocial(RestoreSocialCommand command) {
		String provider = command.getProvider() == null ? "" : command.getProvider().trim().toLowerCase();
		String accessToken = command.getAccessToken();

		if (provider.isBlank() || accessToken == null || accessToken.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		OAuthUserInfo userInfo = oAuthUserInfoPort.getUserInfo(provider, accessToken);

		UserAuth userAuth = userAuthPersistencePort
				.findByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "복구 대상 계정이 아닙니다."));

		User user = userPersistencePort.findById(userAuth.getUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "복구 대상 계정이 아닙니다."));

		return restoreAndIssueTokens(user);
	}

	private LoginResult restoreAndIssueTokens(User user) {
		try {
			user.restore(LocalDateTime.now());
		} catch (UserDomainException exception) {
			throw userDomainExceptionMapper.toBusinessException(exception);
		}

		userPersistencePort.save(user);

		AuthTokenResult tokens = authTokenService.issueTokens(user.getUserUuid());
		return LoginResult.of(user.getUserUuid().toString(), tokens);
	}
}
