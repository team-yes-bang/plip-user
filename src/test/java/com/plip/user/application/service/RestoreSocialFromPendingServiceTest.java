package com.plip.user.application.service;

import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.in.RestoreSocialFromPendingCommand;
import com.plip.user.application.port.in.RestoreSocialCommand;
import com.plip.user.application.port.in.RestoreUserUseCase;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RestoreSocialFromPendingServiceTest {

	@InjectMocks
	private RestoreSocialFromPendingService restoreSocialFromPendingService;

	@Mock
	private SocialSignupPendingPort socialSignupPendingPort;

	@Mock
	private RestoreUserUseCase restoreUserUseCase;

	@Test
	@DisplayName("pending consume 후 소셜 복구 API 호출")
	void restore_success() {
		given(socialSignupPendingPort.findByToken("pending-token")).willReturn(Optional.of(
				new SocialSignupPendingPort.SocialSignupPendingRecord("google", "oauth-token")
		));
		LoginResult loginResult = LoginResult.of("user-uuid", null);
		given(restoreUserUseCase.restoreSocial(any(RestoreSocialCommand.class))).willReturn(loginResult);

		LoginResult result = restoreSocialFromPendingService.restore(
				RestoreSocialFromPendingCommand.of("pending-token")
		);

		assertThat(result).isEqualTo(loginResult);
		then(socialSignupPendingPort).should().deleteByToken("pending-token");

		var commandCaptor = org.mockito.ArgumentCaptor.forClass(RestoreSocialCommand.class);
		then(restoreUserUseCase).should().restoreSocial(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getProvider()).isEqualTo("google");
		assertThat(commandCaptor.getValue().getAccessToken()).isEqualTo("oauth-token");
	}

	@Test
	@DisplayName("pending 만료 또는 미존재 시 예외")
	void restore_pendingNotFound() {
		given(socialSignupPendingPort.findByToken("missing")).willReturn(Optional.empty());

		assertThatThrownBy(() -> restoreSocialFromPendingService.restore(
				RestoreSocialFromPendingCommand.of("missing")
		))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.VERIFICATION_TOKEN_INVALID);

		then(restoreUserUseCase).should(never()).restoreSocial(any());
	}
}
