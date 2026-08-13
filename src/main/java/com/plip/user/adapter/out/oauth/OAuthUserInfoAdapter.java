package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthUserInfoAdapter implements OAuthUserInfoPort {

	private final Map<String, OAuthClient> oauthClients;

	@Override
	public OAuthUserInfo getUserInfo(String provider, String accessToken) {
		String key = provider.toLowerCase() + "OAuthClient";
		OAuthClient client = oauthClients.get(key);
		if (client == null) {
			throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_NOT_SUPPORTED);
		}
		return client.getUserInfo(accessToken);
	}
}
