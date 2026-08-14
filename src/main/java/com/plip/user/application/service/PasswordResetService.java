package com.plip.user.application.service;

import com.plip.user.application.port.in.PasswordResetCommand;
import com.plip.user.application.port.in.PasswordResetUseCase;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
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
public class PasswordResetService implements PasswordResetUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";
	private static final String STATUS_ACTIVE = "ACTIVE";

	private final VerificationTokenPort verificationTokenPort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserPersistencePort userPersistencePort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final AuthTokenService authTokenService;

	@Override
	@Transactional
	public void resetPassword(PasswordResetCommand command) {
		validateVerificationToken(command.getEmail(), command.getVerificationToken());
		validatePassword(command.getNewPassword());

		UserAuth userAuth = userAuthPersistencePort.findByEmailAndAuthType(command.getEmail(), AUTH_TYPE_LOCAL)
				.orElseThrow(() -> new BusinessException(ErrorCode.LOCAL_ACCOUNT_NOT_FOUND));

		User user = userPersistencePort.findById(userAuth.getUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeletedAt() != null || !STATUS_ACTIVE.equals(user.getStatus())) {
			throw new BusinessException(ErrorCode.USER_INACTIVE);
		}

		if (passwordEncoderPort.matches(command.getNewPassword(), userAuth.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_CURRENT);
		}

		String encodedPassword = passwordEncoderPort.encode(command.getNewPassword());
		userAuthPersistencePort.updatePasswordHash(userAuth.getId(), encodedPassword);
		verificationTokenPort.deleteByEmail(OtpPurpose.PASSWORD_RESET, command.getEmail());
		authTokenService.revokeTokens(user.getUserUuid());
	}

	private void validateVerificationToken(String email, String token) {
		String storedToken = verificationTokenPort.findByEmail(OtpPurpose.PASSWORD_RESET, email);
		if (storedToken == null || !storedToken.equals(token)) {
			throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_INVALID);
		}
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8 || password.length() > 64) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "password: 비밀번호는 8~64자여야 합니다.");
		}
	}
}
