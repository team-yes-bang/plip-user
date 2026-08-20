package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component("googleOAuthClient")
public class GoogleOAuthClient implements OAuthClient {

	private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

	private final RestClient restClient;

	public GoogleOAuthClient() {
		this.restClient = RestClient.builder()
				.baseUrl(USERINFO_URL)
				.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			Map<String, Object> response = restClient.get()
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.body(Map.class);

			if (response == null) {
				throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
			}

			return new OAuthUserInfo(
					"google",
					(String) response.get("sub"),
					(String) response.get("email"),
					(String) response.get("name")
			);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("Google OAuth 사용자 정보 조회 실패", e);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}
}
