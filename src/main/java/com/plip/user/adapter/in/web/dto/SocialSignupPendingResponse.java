package com.plip.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "소셜 가입 pending 저장 응답")
public class SocialSignupPendingResponse {

	@Schema(description = "약관 동의 완료 전까지 유지되는 pending 토큰")
	private final String pendingToken;

	@Schema(description = "pending 토큰 TTL(초)")
	private final long expiresInSeconds;

	public static SocialSignupPendingResponse of(String pendingToken, long expiresInSeconds) {
		return new SocialSignupPendingResponse(pendingToken, expiresInSeconds);
	}
}
