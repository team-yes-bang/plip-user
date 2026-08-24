package com.plip.user.application.service;

import com.plip.user.application.port.in.SaveSocialSignupPendingCommand;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.config.OtpProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SocialSignupPendingServiceTest {

	@InjectMocks
	private SocialSignupPendingService socialSignupPendingService;

	@Mock
	private SocialSignupPendingPort socialSignupPendingPort;

	@Mock
	private OtpProperties otpProperties;

	@Test
	@DisplayName("pending 저장 시 UUID 토큰과 TTL 반환")
	void savePending_success() {
		given(otpProperties.getVerificationTokenTtlSeconds()).willReturn(600L);

		var result = socialSignupPendingService.savePending(
				SaveSocialSignupPendingCommand.of("kakao", "oauth-token")
		);

		assertThat(result.getPendingToken()).isNotBlank();
		assertThat(result.getExpiresInSeconds()).isEqualTo(600L);

		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		then(socialSignupPendingPort).should().save(
				tokenCaptor.capture(),
				eq("kakao"),
				eq("oauth-token"),
				eq(600L)
		);
		assertThat(tokenCaptor.getValue()).isEqualTo(result.getPendingToken());
	}

	@Test
	@DisplayName("provider 또는 accessToken 누락 시 INVALID_INPUT")
	void savePending_invalidInput() {
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> socialSignupPendingService.savePending(
				SaveSocialSignupPendingCommand.of("", "oauth-token")
		))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}
}
