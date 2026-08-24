package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.CompleteSocialSignupCommand;
import com.plip.user.application.port.in.LocalSignupCommand;
import com.plip.user.application.port.in.SignupResult;
import com.plip.user.application.port.in.SocialLoginCommand;
import com.plip.user.application.port.in.SocialLoginUseCase;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CompleteSocialSignupServiceTest {

	@InjectMocks
	private CompleteSocialSignupService completeSocialSignupService;

	@Mock
	private SocialSignupPendingPort socialSignupPendingPort;

	@Mock
	private SocialLoginUseCase socialLoginUseCase;

	@Test
	@DisplayName("pending consume 후 소셜 로그인으로 가입 완료")
	void complete_success() {
		given(socialSignupPendingPort.findByToken("pending-token")).willReturn(Optional.of(
				new SocialSignupPendingPort.SocialSignupPendingRecord("google", "oauth-token")
		));

		String userUuid = UUID.randomUUID().toString();
		AuthTokenResult tokens = AuthTokenResult.of("access", "refresh", 3600L);
		given(socialLoginUseCase.login(any(SocialLoginCommand.class)))
				.willReturn(SignupResult.of(userUuid, true, tokens));

		SignupResult result = completeSocialSignupService.complete(
				CompleteSocialSignupCommand.of(
						"pending-token",
						List.of(LocalSignupCommand.TermAgreementItem.of(1L, true))
				)
		);

		assertThat(result.getUserUuid()).isEqualTo(userUuid);
		assertThat(result.isNewUser()).isTrue();

		then(socialSignupPendingPort).should().deleteByToken("pending-token");

		ArgumentCaptor<SocialLoginCommand> commandCaptor = ArgumentCaptor.forClass(SocialLoginCommand.class);
		then(socialLoginUseCase).should().login(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getProvider()).isEqualTo("google");
		assertThat(commandCaptor.getValue().getAccessToken()).isEqualTo("oauth-token");
		assertThat(commandCaptor.getValue().getTermsAgreements()).hasSize(1);
	}

	@Test
	@DisplayName("pending 만료 또는 미존재 시 예외")
	void complete_pendingNotFound() {
		given(socialSignupPendingPort.findByToken("missing")).willReturn(Optional.empty());

		assertThatThrownBy(() -> completeSocialSignupService.complete(
				CompleteSocialSignupCommand.of("missing", List.of())
		))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.VERIFICATION_TOKEN_INVALID);

		then(socialSignupPendingPort).should(never()).deleteByToken("missing");
		then(socialLoginUseCase).should(never()).login(any());
	}
}
