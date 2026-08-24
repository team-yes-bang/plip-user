package com.plip.user.application.service;

import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.in.RestoreSocialCommand;
import com.plip.user.application.port.in.RestoreSocialFromPendingCommand;
import com.plip.user.application.port.in.RestoreSocialFromPendingUseCase;
import com.plip.user.application.port.in.RestoreUserUseCase;
import com.plip.user.application.port.out.SocialSignupPendingPort;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestoreSocialFromPendingService implements RestoreSocialFromPendingUseCase {

	private final SocialSignupPendingPort socialSignupPendingPort;
	private final RestoreUserUseCase restoreUserUseCase;

	@Override
	public LoginResult restore(RestoreSocialFromPendingCommand command) {
		SocialSignupPendingPort.SocialSignupPendingRecord pending = consumePending(command.getPendingToken());
		return restoreUserUseCase.restoreSocial(
				RestoreSocialCommand.of(pending.provider(), pending.accessToken())
		);
	}

	private SocialSignupPendingPort.SocialSignupPendingRecord consumePending(String pendingToken) {
		if (pendingToken == null || pendingToken.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 복구 정보가 없습니다.");
		}

		SocialSignupPendingPort.SocialSignupPendingRecord record = socialSignupPendingPort
				.findByToken(pendingToken.trim())
				.orElseThrow(() -> new BusinessException(
						ErrorCode.VERIFICATION_TOKEN_INVALID,
						"소셜 복구 정보가 만료되었거나 존재하지 않습니다."
				));

		socialSignupPendingPort.deleteByToken(pendingToken.trim());
		return record;
	}
}
