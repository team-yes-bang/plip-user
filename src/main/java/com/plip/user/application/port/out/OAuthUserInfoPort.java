package com.plip.user.application.port.out;

public interface OAuthUserInfoPort {

	OAuthUserInfo getUserInfo(String provider, String accessToken);

	record OAuthUserInfo(
			String provider,
			String providerUserId,
			String email,
			String nickname
	) {}
}
