package com.plip.user.application.service;

import com.plip.user.application.port.in.PasswordResetCommand;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	@InjectMocks
	private PasswordResetService passwordResetService;

	@Mock private VerificationTokenPort verificationTokenPort;
	@Mock private UserAuthPersistencePort userAuthPersistencePort;
	@Mock private UserPersistencePort userPersistencePort;
	@Mock private PasswordEncoderPort passwordEncoderPort;
	@Mock private AuthTokenService authTokenService;

	private static final String EMAIL = "user@example.com";
	private static final String TOKEN = "verification-token";
	private static final String OLD_PASSWORD = "oldPassword1!";
	private static final String NEW_PASSWORD = "newPassword1!";
	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@Test
	@DisplayName("기존 비밀번호와 동일하면 재설정 실패")
	void resetPassword_same_password() {
		given(verificationTokenPort.findByEmail(OtpPurpose.PASSWORD_RESET, EMAIL)).willReturn(TOKEN);
		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(
				UserAuth.of(1L, 1L, "LOCAL", EMAIL, "encoded_old", null, null, null, null, null)
		));
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(
				User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", null, null, null)
		));
		given(passwordEncoderPort.matches(NEW_PASSWORD, "encoded_old")).willReturn(true);

		assertThatThrownBy(() -> passwordResetService.resetPassword(
				PasswordResetCommand.of(EMAIL, TOKEN, NEW_PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.PASSWORD_SAME_AS_CURRENT);
	}

	@Test
	@DisplayName("비밀번호 재설정 성공")
	void resetPassword_success() {
		given(verificationTokenPort.findByEmail(OtpPurpose.PASSWORD_RESET, EMAIL)).willReturn(TOKEN);
		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(
				UserAuth.of(1L, 1L, "LOCAL", EMAIL, "encoded_old", null, null, null, null, null)
		));
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(
				User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", null, null, null)
		));
		given(passwordEncoderPort.matches(NEW_PASSWORD, "encoded_old")).willReturn(false);
		given(passwordEncoderPort.encode(NEW_PASSWORD)).willReturn("encoded_new");

		passwordResetService.resetPassword(PasswordResetCommand.of(EMAIL, TOKEN, NEW_PASSWORD));

		then(userAuthPersistencePort).should().updatePasswordHash(1L, "encoded_new");
		then(verificationTokenPort).should().deleteByEmail(OtpPurpose.PASSWORD_RESET, EMAIL);
		then(authTokenService).should().revokeTokens(USER_UUID);
	}
}
