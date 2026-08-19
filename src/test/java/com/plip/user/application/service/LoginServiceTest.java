package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LocalLoginCommand;
import com.plip.user.application.port.in.LoginResult;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	@InjectMocks
	private LoginService loginService;

	@Mock private UserAuthPersistencePort userAuthPersistencePort;
	@Mock private UserPersistencePort userPersistencePort;
	@Mock private PasswordEncoderPort passwordEncoderPort;
	@Mock private AuthTokenService authTokenService;
	@Mock private UserAccountStatusValidator userAccountStatusValidator;

	private static final String EMAIL = "user@example.com";
	private static final String PASSWORD = "password123!";
	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final AuthTokenResult TOKEN_RESULT = AuthTokenResult.of("access", "refresh", 3600);

	@Test
	@DisplayName("로컬 로그인 성공")
	void login_success() {
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null);
		User user = User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded_password")).willReturn(true);
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(user));
		org.mockito.Mockito.doNothing().when(userAccountStatusValidator).validateLoginEligible(user);
		given(authTokenService.issueTokens(USER_UUID)).willReturn(TOKEN_RESULT);

		LoginResult result = loginService.login(LocalLoginCommand.of(EMAIL, PASSWORD));

		assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
		assertThat(result.getTokens()).isEqualTo(TOKEN_RESULT);
	}

	@Test
	@DisplayName("비밀번호 불일치 시 실패")
	void login_invalid_password() {
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null);
		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded_password")).willReturn(false);

		assertThatThrownBy(() -> loginService.login(LocalLoginCommand.of(EMAIL, PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_CREDENTIALS);
	}

	@Test
	@DisplayName("탈퇴 유예 계정 로그인 실패 - USER_DELETED_RESTORABLE")
	void login_deleted_user() {
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null);
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(),
				LocalDateTime.now());

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded_password")).willReturn(true);
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(user));
		org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.USER_DELETED_RESTORABLE))
				.when(userAccountStatusValidator).validateLoginEligible(user);

		assertThatThrownBy(() -> loginService.login(LocalLoginCommand.of(EMAIL, PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_DELETED_RESTORABLE);
	}

}
