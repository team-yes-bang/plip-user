package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.PasswordChangeCommand;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {

	@InjectMocks
	private PasswordChangeService passwordChangeService;

	@Mock private UserPersistencePort userPersistencePort;
	@Mock private UserAuthPersistencePort userAuthPersistencePort;
	@Mock private PasswordEncoderPort passwordEncoderPort;
	@Mock private AuthTokenService authTokenService;
	@Mock private UserAccountStatusValidator userAccountStatusValidator;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final String CURRENT_PASSWORD = "currentPass1!";
	private static final String NEW_PASSWORD = "newPassword1!";
	private static final AuthTokenResult TOKEN_RESULT = AuthTokenResult.of("new-access", "new-refresh", 3600);

	@Test
	@DisplayName("현재 비밀번호 불일치 시 AUTH_009")
	void changePassword_wrong_current_password() {
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(
				User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", null, null, null)
		));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.of(
				UserAuth.of(1L, 1L, "LOCAL", "user@example.com", "encoded", null, null, null, null, null)
		));
		given(passwordEncoderPort.matches(CURRENT_PASSWORD, "encoded")).willReturn(false);

		assertThatThrownBy(() -> passwordChangeService.changePassword(
				PasswordChangeCommand.of(USER_UUID.toString(), CURRENT_PASSWORD, NEW_PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException ex = (BusinessException) e;
					assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CURRENT_PASSWORD_MISMATCH);
					assertThat(ex.getMessage()).isEqualTo("현재 비밀번호가 올바르지 않습니다.");
				});
	}

	@Test
	@DisplayName("비밀번호 변경 성공 시 refresh 파기 및 새 JWT 발급")
	void changePassword_success() {
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(
				User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", null, null, null)
		));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.of(
				UserAuth.of(1L, 1L, "LOCAL", "user@example.com", "encoded", null, null, null, null, null)
		));
		given(passwordEncoderPort.matches(CURRENT_PASSWORD, "encoded")).willReturn(true);
		given(passwordEncoderPort.matches(NEW_PASSWORD, "encoded")).willReturn(false);
		given(passwordEncoderPort.encode(NEW_PASSWORD)).willReturn("encoded_new");
		given(authTokenService.issueTokens(USER_UUID)).willReturn(TOKEN_RESULT);

		AuthTokenResult result = passwordChangeService.changePassword(
				PasswordChangeCommand.of(USER_UUID.toString(), CURRENT_PASSWORD, NEW_PASSWORD));

		assertThat(result.getAccessToken()).isEqualTo("new-access");
		then(userAuthPersistencePort).should().updatePasswordHash(1L, "encoded_new");
		then(authTokenService).should().revokeTokens(USER_UUID);
		then(authTokenService).should().issueTokens(USER_UUID);
	}
}
