package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component("naverOAuthClient")
public class NaverOAuthClient implements OAuthClient {

	private static final String USERINFO_URL = "https://openapi.naver.com/v1/nid/me";

	private final RestClient restClient;

	public NaverOAuthClient() {
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

			Map<String, Object> naverResponse = (Map<String, Object>) response.get("response");
			if (naverResponse == null) {
				throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
			}

			return new OAuthUserInfo(
					"naver",
					(String) naverResponse.get("id"),
					(String) naverResponse.get("email"),
					(String) naverResponse.get("nickname"),
					(String) naverResponse.get("profile_image")
			);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("Naver OAuth 사용자 정보 조회 실패", e);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}
}
