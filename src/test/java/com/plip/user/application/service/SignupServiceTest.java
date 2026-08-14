package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LocalSignupCommand;
import com.plip.user.application.port.in.LocalSignupCommand.TermAgreementItem;
import com.plip.user.application.port.in.SignupResult;
import com.plip.user.application.port.in.SocialLoginCommand;
import com.plip.user.application.port.out.EventPublisherPort;
import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.application.port.out.UuidGeneratorPort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

	@InjectMocks
	private SignupService signupService;

	@Mock private VerificationTokenPort verificationTokenPort;
	@Mock private PasswordEncoderPort passwordEncoderPort;
	@Mock private UserPersistencePort userPersistencePort;
	@Mock private UserAuthPersistencePort userAuthPersistencePort;
	@Mock private TermPersistencePort termPersistencePort;
	@Mock private UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;
	@Mock private UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;
	@Mock private UuidGeneratorPort uuidGeneratorPort;
	@Mock private OAuthUserInfoPort oAuthUserInfoPort;
	@Mock private EventPublisherPort eventPublisherPort;
	@Mock private AuthTokenService authTokenService;
	@Mock private UserAccountStatusValidator userAccountStatusValidator;

	private static final String EMAIL = "test@example.com";
	private static final String TOKEN = "valid-token";
	private static final String PASSWORD = "password123!";
	private static final String NICKNAME = "테스트유저";
	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final AuthTokenResult TOKEN_RESULT = AuthTokenResult.of("access", "refresh", 3600);

	@BeforeEach
	void setUpTokens() {
		lenient().when(authTokenService.issueTokens(any(UuidV7.class))).thenReturn(TOKEN_RESULT);
	}

	@Nested
	@DisplayName("이메일 회원가입")
	class LocalSignupTest {

		@Test
		@DisplayName("정상 가입 성공")
		void signup_success() {
			// given
			List<TermAgreementItem> termAgreements = List.of(
					TermAgreementItem.of(1L, true),
					TermAgreementItem.of(2L, true)
			);
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME, termAgreements);

			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null),
					Term.of(2L, null, "개인정보 처리방침", "/terms/privacy", "PRIVACY", "v1.0", false, "ACTIVE", null, null)
			));
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);
			given(passwordEncoderPort.encode(PASSWORD)).willReturn("encoded_password");

			User savedUser = User.of(1L, USER_UUID, NICKNAME, null, "ACTIVE",
					LocalDateTime.now(), LocalDateTime.now(), null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null)
			);
			given(userTermsAgreementPersistencePort.saveAll(anyList())).willReturn(Collections.emptyList());
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			// when
			SignupResult result = signupService.signup(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
			assertThat(result.isNewUser()).isTrue();
			assertThat(result.getTokens()).isEqualTo(TOKEN_RESULT);
			then(verificationTokenPort).should().deleteByEmail(OtpPurpose.SIGNUP, EMAIL);
			then(eventPublisherPort).should().publish(eq("user.registered"), anyString(), anyString());
		}

		@Test
		@DisplayName("인증 토큰이 만료(없음)되면 실패")
		void signup_expired_token() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME,
					List.of(TermAgreementItem.of(1L, true)));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(null);

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.VERIFICATION_TOKEN_INVALID);
		}

		@Test
		@DisplayName("인증 토큰이 불일치하면 실패")
		void signup_invalid_token() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, "wrong-token", PASSWORD, NICKNAME,
					List.of(TermAgreementItem.of(1L, true)));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.VERIFICATION_TOKEN_INVALID);
		}

		@Test
		@DisplayName("필수 약관이 없으면 약관 동의 없이도 가입 성공")
		void signup_success_without_required_terms() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME, Collections.emptyList());

			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(Collections.emptyList());
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);
			given(passwordEncoderPort.encode(PASSWORD)).willReturn("encoded_password");

			User savedUser = User.of(1L, USER_UUID, NICKNAME, null, "ACTIVE", null, null, null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null)
			);
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			// when
			SignupResult result = signupService.signup(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
			then(userTermsAgreementPersistencePort).should(never()).saveAll(anyList());
		}

		@Test
		@DisplayName("이미 가입된 이메일(LOCAL)이면 실패")
		void signup_duplicate_email() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME,
					List.of(TermAgreementItem.of(1L, true)));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(
					UserAuth.of(1L, 1L, "LOCAL", EMAIL, "hash", null, null, null, null, null)
			));

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);
		}

		@Test
		@DisplayName("동일 이메일이 소셜로 존재해도 로컬 가입 가능")
		void signup_same_email_different_auth_type() {
			// given
			List<TermAgreementItem> termAgreements = List.of(TermAgreementItem.of(1L, true));
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME, termAgreements);

			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);
			given(passwordEncoderPort.encode(PASSWORD)).willReturn("encoded_password");

			User savedUser = User.of(1L, USER_UUID, NICKNAME, null, "ACTIVE", null, null, null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "LOCAL", EMAIL, "encoded_password", null, null, null, null, null)
			);
			given(userTermsAgreementPersistencePort.saveAll(anyList())).willReturn(Collections.emptyList());
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			// when
			SignupResult result = signupService.signup(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
		}

		@Test
		@DisplayName("필수 약관 미동의 시 실패")
		void signup_required_terms_not_agreed() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME,
					List.of(TermAgreementItem.of(2L, true)));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(2L, null, "마케팅 동의", "/terms/marketing", "MARKETING", "v1.0", false, "ACTIVE", null, null)
			));

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
		}

		@Test
		@DisplayName("DEPRECATED 약관에 동의하면 실패")
		void signup_deprecated_term_rejected() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME,
					List.of(
							TermAgreementItem.of(1L, true),
							TermAgreementItem.of(9L, true)
					));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null),
					Term.of(9L, null, "마케팅 수신 동의 (구버전)", "/terms/marketing/v0.9", "MARKETING", "v0.9", false, "DEPRECATED", null, null)
			));

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.TERM_NOT_FOUND);
		}

		@Test
		@DisplayName("존재하지 않는 약관 ID로 동의하면 실패")
		void signup_nonexistent_term_rejected() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, NICKNAME,
					List.of(
							TermAgreementItem.of(1L, true),
							TermAgreementItem.of(999L, true)
					));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);
			given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.TERM_NOT_FOUND);
		}

		@Test
		@DisplayName("닉네임이 2자 미만이면 실패")
		void signup_invalid_nickname_short() {
			// given
			LocalSignupCommand command = LocalSignupCommand.of(
					EMAIL, TOKEN, PASSWORD, "A",
					List.of(TermAgreementItem.of(1L, true)));
			given(verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, EMAIL)).willReturn(TOKEN);

			// when & then
			assertThatThrownBy(() -> signupService.signup(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.INVALID_NICKNAME);
		}
	}

	@Nested
	@DisplayName("소셜 로그인")
	class SocialLoginTest {

		@Test
		@DisplayName("기존 소셜 사용자 로그인 성공")
		void social_login_existing_user() {
			// given
			SocialLoginCommand command = SocialLoginCommand.of(
					"google", "access-token", null);

			OAuthUserInfo userInfo = new OAuthUserInfo(
					"google", "google-id-123", "social@example.com", "GoogleUser", null);
			given(oAuthUserInfoPort.getUserInfo("google", "access-token")).willReturn(userInfo);

			UserAuth existingAuth = UserAuth.of(
					1L, 1L, "SOCIAL", "social@example.com", null,
					"google", "google-id-123", null, null, null);
			given(userAuthPersistencePort.findByProviderAndProviderUserId("google", "google-id-123"))
					.willReturn(Optional.of(existingAuth));

			User existingUser = User.of(1L, USER_UUID, "기존유저", null, "ACTIVE", null, null, null);
			given(userPersistencePort.findById(1L)).willReturn(Optional.of(existingUser));
			org.mockito.Mockito.doNothing().when(userAccountStatusValidator).validateLoginEligible(existingUser);

			// when
			SignupResult result = signupService.login(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
			assertThat(result.isNewUser()).isFalse();
			then(eventPublisherPort).should(never()).publish(anyString(), anyString(), anyString());
		}

		@Test
		@DisplayName("신규 소셜 사용자 - 닉네임/프로필은 프로바이더에서 수집")
		void social_login_new_user_with_provider_info() {
			// given
			List<TermAgreementItem> terms = List.of(TermAgreementItem.of(1L, true));
			SocialLoginCommand command = SocialLoginCommand.of(
					"kakao", "kakao-token", terms);

			OAuthUserInfo userInfo = new OAuthUserInfo(
					"kakao", "kakao-id-456", "kakao@example.com",
					"카카오닉네임", "https://kakao.com/profile.jpg");
			given(oAuthUserInfoPort.getUserInfo("kakao", "kakao-token")).willReturn(userInfo);
			given(userAuthPersistencePort.findByProviderAndProviderUserId("kakao", "kakao-id-456"))
					.willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);

			User savedUser = User.of(1L, USER_UUID, "카카오닉네임",
					"https://kakao.com/profile.jpg", "ACTIVE", null, null, null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "SOCIAL", "kakao@example.com", null,
							"kakao", "kakao-id-456", null, null, null)
			);
			given(userTermsAgreementPersistencePort.saveAll(anyList())).willReturn(Collections.emptyList());
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			// when
			SignupResult result = signupService.login(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
			assertThat(result.isNewUser()).isTrue();
			then(eventPublisherPort).should().publish(eq("user.registered"), anyString(), anyString());
		}

		@Test
		@DisplayName("신규 소셜 사용자 - 12자 초과 닉네임은 잘림")
		void social_login_new_user_nickname_truncated() {
			List<TermAgreementItem> terms = List.of(TermAgreementItem.of(1L, true));
			SocialLoginCommand command = SocialLoginCommand.of("kakao", "kakao-token", terms);

			OAuthUserInfo userInfo = new OAuthUserInfo(
					"kakao", "kakao-id-long", "kakao@example.com",
					"VeryLongNicknameFromProvider", null);
			given(oAuthUserInfoPort.getUserInfo("kakao", "kakao-token")).willReturn(userInfo);
			given(userAuthPersistencePort.findByProviderAndProviderUserId("kakao", "kakao-id-long"))
					.willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(termPersistencePort.findAllByIdIn(anyList())).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);

			User savedUser = User.of(1L, USER_UUID, "VeryLongNick", null, "ACTIVE", null, null, null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "SOCIAL", "kakao@example.com", null,
							"kakao", "kakao-id-long", null, null, null)
			);
			given(userTermsAgreementPersistencePort.saveAll(anyList())).willReturn(Collections.emptyList());
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			signupService.login(command);

			org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
			verify(userPersistencePort).save(userCaptor.capture());
			assertThat(userCaptor.getValue().getNickname()).isEqualTo("VeryLongNick");
		}

		@Test
		@DisplayName("필수 약관이 있을 때 신규 소셜 사용자가 약관 없이 요청하면 SOCIAL_SIGNUP_REQUIRED")
		void social_login_new_user_without_terms_when_required_exists() {
			// given
			SocialLoginCommand command = SocialLoginCommand.of(
					"google", "access-token", null);

			OAuthUserInfo userInfo = new OAuthUserInfo(
					"google", "new-google-id", "new@example.com", "NewUser", null);
			given(oAuthUserInfoPort.getUserInfo("google", "access-token")).willReturn(userInfo);
			given(userAuthPersistencePort.findByProviderAndProviderUserId("google", "new-google-id"))
					.willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(List.of(
					Term.of(1L, null, "서비스 이용약관", "/terms/service", "SERVICE", "v1.0", true, "ACTIVE", null, null)
			));

			// when & then
			assertThatThrownBy(() -> signupService.login(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.SOCIAL_SIGNUP_REQUIRED);
		}

		@Test
		@DisplayName("필수 약관이 없으면 약관 없이도 소셜 가입 성공")
		void social_login_new_user_without_terms_when_no_required() {
			// given
			SocialLoginCommand command = SocialLoginCommand.of(
					"naver", "naver-token", null);

			OAuthUserInfo userInfo = new OAuthUserInfo(
					"naver", "naver-id-789", "naver@example.com", "네이버유저", null);
			given(oAuthUserInfoPort.getUserInfo("naver", "naver-token")).willReturn(userInfo);
			given(userAuthPersistencePort.findByProviderAndProviderUserId("naver", "naver-id-789"))
					.willReturn(Optional.empty());
			given(termPersistencePort.findAllActiveRequired()).willReturn(Collections.emptyList());
			given(uuidGeneratorPort.generate()).willReturn(USER_UUID);

			User savedUser = User.of(1L, USER_UUID, "네이버유저", null, "ACTIVE", null, null, null);
			given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
			given(userAuthPersistencePort.save(any(UserAuth.class))).willReturn(
					UserAuth.of(1L, 1L, "SOCIAL", "naver@example.com", null,
							"naver", "naver-id-789", null, null, null)
			);
			given(userNotificationSettingPersistencePort.save(any())).willReturn(null);

			// when
			SignupResult result = signupService.login(command);

			// then
			assertThat(result.getUserUuid()).isEqualTo(USER_UUID.toString());
			assertThat(result.isNewUser()).isTrue();
			then(userTermsAgreementPersistencePort).should(never()).saveAll(anyList());
		}
	}
}
