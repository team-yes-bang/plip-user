package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.PasswordChangeCommand;
import com.plip.user.application.port.in.PasswordChangeUseCase;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordChangeService implements PasswordChangeUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserPersistencePort userPersistencePort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final AuthTokenService authTokenService;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public AuthTokenResult changePassword(PasswordChangeCommand command) {
		validatePassword(command.getNewPassword());

		UuidV7 userUuid = UuidV7.parse(command.getUserUuid());
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		UserAuth userAuth = userAuthPersistencePort.findByUserIdAndAuthType(user.getId(), AUTH_TYPE_LOCAL)
				.orElseThrow(() -> new BusinessException(ErrorCode.LOCAL_ACCOUNT_NOT_FOUND));

		if (!passwordEncoderPort.matches(command.getCurrentPassword(), userAuth.getPasswordHash())) {
			throw new BusinessException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
		}

		if (passwordEncoderPort.matches(command.getNewPassword(), userAuth.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_CURRENT);
		}

		String encodedPassword = passwordEncoderPort.encode(command.getNewPassword());
		userAuthPersistencePort.updatePasswordHash(userAuth.getId(), encodedPassword);
		authTokenService.revokeTokens(userUuid);
		return authTokenService.issueTokens(userUuid);
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8 || password.length() > 64) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "newPassword: 비밀번호는 8~64자여야 합니다.");
		}
	}
}
