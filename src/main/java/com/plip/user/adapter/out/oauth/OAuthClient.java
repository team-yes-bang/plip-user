package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;

public interface OAuthClient {

	OAuthUserInfo getUserInfo(String accessToken);
}
