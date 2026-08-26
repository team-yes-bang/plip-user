package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component("kakaoOAuthClient")
public class KakaoOAuthClient implements OAuthClient {

	private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

	private final RestClient restClient;

	public KakaoOAuthClient() {
		this(createDefaultRestClient());
	}

	KakaoOAuthClient(RestClient restClient) {
		this.restClient = restClient;
	}

	private static RestClient createDefaultRestClient() {
		return RestClient.builder()
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

			String providerUserId = OAuthProviderUserIdValidator.requireProviderUserId(response.get("id"));
			Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
			Map<String, Object> profile = kakaoAccount != null
					? (Map<String, Object>) kakaoAccount.get("profile")
					: null;

			String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
			String nickname = profile != null ? (String) profile.get("nickname") : null;

			return new OAuthUserInfo("kakao", providerUserId, email, nickname);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("Kakao OAuth 사용자 정보 조회 실패", e);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}
}
