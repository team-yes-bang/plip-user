package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.in.RestoreLocalCommand;
import com.plip.user.application.port.in.RestoreSocialCommand;
import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RestoreUserServiceTest {

	@Mock private UserAuthPersistencePort userAuthPersistencePort;
	@Mock private UserPersistencePort userPersistencePort;
	@Mock private PasswordEncoderPort passwordEncoderPort;
	@Mock private OAuthUserInfoPort oAuthUserInfoPort;
	@Mock private AuthTokenService authTokenService;

	private RestoreUserService restoreUserService;

	private static final String EMAIL = "user2@plip.test";
	private static final String PASSWORD = "password";
	private static final String PROVIDER = "kakao";
	private static final String SOCIAL_ACCESS_TOKEN = "kakao-access-token";
	private static final String PROVIDER_USER_ID = "kakao-user-123";
	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final AuthTokenResult TOKEN_RESULT = AuthTokenResult.of("access", "refresh", 3600);

	@BeforeEach
	void setUp() {
		restoreUserService = new RestoreUserService(
				userAuthPersistencePort,
				userPersistencePort,
				passwordEncoderPort,
				oAuthUserInfoPort,
				authTokenService,
				new UserDomainExceptionMapper()
		);
	}

	@Test
	@DisplayName("유예 기간 내 DELETED 로컬 계정 복구 성공 시 JWT 발급")
	void restoreLocal_success() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(10);
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded", null, null, null, null, null);
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(), deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded")).willReturn(true);
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(authTokenService.issueTokens(USER_UUID)).willReturn(TOKEN_RESULT);

		LoginResult result = restoreUserService.restoreLocal(RestoreLocalCommand.of(EMAIL, PASSWORD));

		assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
		assertThat(result.getTokens()).isEqualTo(TOKEN_RESULT);
		then(userPersistencePort).should().save(any(User.class));
	}

	@Test
	@DisplayName("유예 기간 만료 로컬 계정 복구 실패")
	void restoreLocal_grace_expired() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded", null, null, null, null, null);
		User user = User.of(1L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(), deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded")).willReturn(true);
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> restoreUserService.restoreLocal(RestoreLocalCommand.of(EMAIL, PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_WITHDRAWAL_GRACE_EXPIRED);
	}

	@Test
	@DisplayName("ACTIVE 로컬 계정 복구 실패")
	void restoreLocal_not_deleted() {
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", EMAIL, "encoded", null, null, null, null, null);
		User user = User.of(1L, USER_UUID, "닉네임", null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), null);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(passwordEncoderPort.matches(PASSWORD, "encoded")).willReturn(true);
		given(userPersistencePort.findById(1L)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> restoreUserService.restoreLocal(RestoreLocalCommand.of(EMAIL, PASSWORD)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}

	@Test
	@DisplayName("유예 기간 내 DELETED 소셜 계정 복구 성공 시 JWT 발급")
	void restoreSocial_success() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(10);
		OAuthUserInfo userInfo = new OAuthUserInfo(PROVIDER, PROVIDER_USER_ID, "social@plip.test", "닉네임", null);
		UserAuth userAuth = UserAuth.of(
				2L, 2L, "SOCIAL", "social@plip.test", null, PROVIDER, PROVIDER_USER_ID, null, null, null);
		User user = User.of(2L, USER_UUID, "닉네임", null, "DELETED", LocalDateTime.now(), LocalDateTime.now(), deletedAt);

		given(oAuthUserInfoPort.getUserInfo(PROVIDER, SOCIAL_ACCESS_TOKEN)).willReturn(userInfo);
		given(userAuthPersistencePort.findByProviderAndProviderUserId(PROVIDER, PROVIDER_USER_ID))
				.willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(2L)).willReturn(Optional.of(user));
		given(userPersistencePort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(authTokenService.issueTokens(USER_UUID)).willReturn(TOKEN_RESULT);

		LoginResult result = restoreUserService.restoreSocial(
				RestoreSocialCommand.of(PROVIDER, SOCIAL_ACCESS_TOKEN));

		assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
		assertThat(result.getTokens()).isEqualTo(TOKEN_RESULT);
		then(userPersistencePort).should().save(any(User.class));
	}

	@Test
	@DisplayName("미가입 소셜 계정 복구 실패")
	void restoreSocial_account_not_found() {
		OAuthUserInfo userInfo = new OAuthUserInfo(PROVIDER, PROVIDER_USER_ID, "social@plip.test", "닉네임", null);

		given(oAuthUserInfoPort.getUserInfo(PROVIDER, SOCIAL_ACCESS_TOKEN)).willReturn(userInfo);
		given(userAuthPersistencePort.findByProviderAndProviderUserId(PROVIDER, PROVIDER_USER_ID))
				.willReturn(Optional.empty());

		assertThatThrownBy(() -> restoreUserService.restoreSocial(
				RestoreSocialCommand.of(PROVIDER, SOCIAL_ACCESS_TOKEN)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}
}
