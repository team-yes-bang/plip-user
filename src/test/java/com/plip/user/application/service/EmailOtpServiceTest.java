package com.plip.user.application.service;

import com.plip.user.application.port.in.EmailOtpRequestCommand;
import com.plip.user.application.port.in.EmailOtpVerifyCommand;
import com.plip.user.application.port.in.EmailOtpVerifyResult;
import com.plip.user.application.port.out.EmailSendPort;
import com.plip.user.application.port.out.OtpPort;
import com.plip.user.application.port.out.RateLimitPort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.global.config.OtpProperties;
import com.plip.user.global.config.RateLimitProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailOtpServiceTest {

	@InjectMocks
	private EmailOtpService emailOtpService;

	@Mock
	private OtpPort otpPort;
	@Mock
	private VerificationTokenPort verificationTokenPort;
	@Mock
	private RateLimitPort rateLimitPort;
	@Mock
	private EmailSendPort emailSendPort;
	@Mock
	private UserAuthPersistencePort userAuthPersistencePort;
	@Mock
	private OtpProperties otpProperties;
	@Mock
	private RateLimitProperties rateLimitProperties;

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_IP = "127.0.0.1";

	private void stubRateLimitNotExceeded() {
		RateLimitProperties.Limit emailLimit = new RateLimitProperties.Limit();
		emailLimit.setMinuteMax(1);
		emailLimit.setDailyMax(5);

		RateLimitProperties.Limit ipLimit = new RateLimitProperties.Limit();
		ipLimit.setMinuteMax(10);
		ipLimit.setDailyMax(100);

		given(rateLimitProperties.resolveEmailLimit(any())).willReturn(emailLimit);
		given(rateLimitProperties.resolveIpLimit(any())).willReturn(ipLimit);
		given(rateLimitPort.isExceeded(anyString(), anyInt(), anyLong())).willReturn(false);
	}

	@Nested
	@DisplayName("OTP 발송 요청")
	class RequestOtp {

		@Test
		@DisplayName("SIGNUP - 정상 발송 시 OTP 저장 및 이메일 발송")
		void requestOtp_signup_success() {
			stubRateLimitNotExceeded();
			given(userAuthPersistencePort.findByEmailAndAuthType(TEST_EMAIL, "LOCAL"))
					.willReturn(Optional.empty());
			given(otpProperties.getLength()).willReturn(6);
			given(otpProperties.getTtlSeconds()).willReturn(120L);

			EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
					TEST_EMAIL, TEST_IP, OtpPurpose.SIGNUP);
			emailOtpService.requestOtp(command);

			verify(otpPort).save(eq(OtpPurpose.SIGNUP), eq(TEST_EMAIL), anyString(), eq(120L));
			verify(emailSendPort).sendOtp(eq(TEST_EMAIL), anyString());
		}

		@Test
		@DisplayName("SIGNUP - 이미 가입된 LOCAL 이메일이면 예외 발생")
		void requestOtp_signup_emailAlreadyRegistered() {
			stubRateLimitNotExceeded();
			given(userAuthPersistencePort.findByEmailAndAuthType(TEST_EMAIL, "LOCAL"))
					.willReturn(Optional.of(
							UserAuth.of(1L, 1L, "LOCAL", TEST_EMAIL, "hash", null, null, null, null, null)
					));

			EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
					TEST_EMAIL, TEST_IP, OtpPurpose.SIGNUP);

			assertThatThrownBy(() -> emailOtpService.requestOtp(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);

			verify(emailSendPort, never()).sendOtp(anyString(), anyString());
		}

		@Test
		@DisplayName("PASSWORD_RESET - LOCAL 계정이 있으면 OTP 발송")
		void requestOtp_passwordReset_success() {
			stubRateLimitNotExceeded();
			given(userAuthPersistencePort.findByEmailAndAuthType(TEST_EMAIL, "LOCAL"))
					.willReturn(Optional.of(
							UserAuth.of(1L, 1L, "LOCAL", TEST_EMAIL, "hash", null, null, null, null, null)
					));
			given(otpProperties.getLength()).willReturn(6);
			given(otpProperties.getTtlSeconds()).willReturn(120L);

			EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
					TEST_EMAIL, TEST_IP, OtpPurpose.PASSWORD_RESET);
			emailOtpService.requestOtp(command);

			verify(otpPort).save(eq(OtpPurpose.PASSWORD_RESET), eq(TEST_EMAIL), anyString(), eq(120L));
			verify(emailSendPort).sendOtp(eq(TEST_EMAIL), anyString());
		}

		@Test
		@DisplayName("PASSWORD_RESET - LOCAL 계정이 없으면 이메일 미발송·성공 처리(계정 열거 방지)")
		void requestOtp_passwordReset_silentSuccessWhenNoAccount() {
			stubRateLimitNotExceeded();
			given(userAuthPersistencePort.findByEmailAndAuthType(TEST_EMAIL, "LOCAL"))
					.willReturn(Optional.empty());

			EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
					TEST_EMAIL, TEST_IP, OtpPurpose.PASSWORD_RESET);
			emailOtpService.requestOtp(command);

			verify(otpPort, never()).save(any(), anyString(), anyString(), anyLong());
			verify(emailSendPort, never()).sendOtp(anyString(), anyString());
		}

		@Test
		@DisplayName("Rate Limit 초과 시 예외 발생")
		void requestOtp_rateLimitExceeded() {
			RateLimitProperties.Limit emailLimit = new RateLimitProperties.Limit();
			emailLimit.setMinuteMax(1);
			emailLimit.setDailyMax(5);

			given(rateLimitProperties.resolveEmailLimit(any())).willReturn(emailLimit);
			given(rateLimitPort.isExceeded(anyString(), anyInt(), anyLong())).willReturn(true);

			EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
					TEST_EMAIL, TEST_IP, OtpPurpose.SIGNUP);

			assertThatThrownBy(() -> emailOtpService.requestOtp(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.OTP_RATE_LIMIT_EXCEEDED);

			verify(emailSendPort, never()).sendOtp(anyString(), anyString());
		}
	}

	@Nested
	@DisplayName("OTP 검증")
	class VerifyOtp {

		@Test
		@DisplayName("정상 검증 시 purpose별 Verification Token 반환")
		void verifyOtp_success() {
			String storedOtp = "123456";
			given(otpPort.findByEmail(OtpPurpose.PASSWORD_RESET, TEST_EMAIL)).willReturn(storedOtp);
			given(otpProperties.getVerificationTokenTtlSeconds()).willReturn(600L);

			EmailOtpVerifyCommand command = EmailOtpVerifyCommand.of(
					TEST_EMAIL, storedOtp, TEST_IP, OtpPurpose.PASSWORD_RESET);
			EmailOtpVerifyResult result = emailOtpService.verifyOtp(command);

			assertThat(result.getVerificationToken()).isNotBlank();
			verify(otpPort).deleteByEmail(OtpPurpose.PASSWORD_RESET, TEST_EMAIL);
			verify(verificationTokenPort).save(
					eq(OtpPurpose.PASSWORD_RESET), eq(TEST_EMAIL), anyString(), eq(600L));
		}

		@Test
		@DisplayName("OTP 만료 또는 미존재 시 예외 발생")
		void verifyOtp_expired() {
			given(otpPort.findByEmail(OtpPurpose.SIGNUP, TEST_EMAIL)).willReturn(null);

			EmailOtpVerifyCommand command = EmailOtpVerifyCommand.of(
					TEST_EMAIL, "123456", TEST_IP, OtpPurpose.SIGNUP);

			assertThatThrownBy(() -> emailOtpService.verifyOtp(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.OTP_EXPIRED_OR_NOT_FOUND);
		}

		@Test
		@DisplayName("OTP 불일치 시 예외 발생")
		void verifyOtp_mismatch() {
			given(otpPort.findByEmail(OtpPurpose.SIGNUP, TEST_EMAIL)).willReturn("123456");

			EmailOtpVerifyCommand command = EmailOtpVerifyCommand.of(
					TEST_EMAIL, "999999", TEST_IP, OtpPurpose.SIGNUP);

			assertThatThrownBy(() -> emailOtpService.verifyOtp(command))
					.isInstanceOf(BusinessException.class)
					.extracting(e -> ((BusinessException) e).getErrorCode())
					.isEqualTo(ErrorCode.OTP_MISMATCH);
		}
	}
}
