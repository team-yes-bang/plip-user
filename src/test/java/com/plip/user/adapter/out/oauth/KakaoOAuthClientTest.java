package com.plip.user.adapter.out.oauth;

import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoOAuthClientTest {

	private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

	private MockRestSetup createClient() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl(USERINFO_URL).build();
		return new MockRestSetup(server, new KakaoOAuthClient(restClient));
	}

	private record MockRestSetup(MockRestServiceServer server, KakaoOAuthClient client) {
	}

	@Test
	@DisplayName("id가 있으면 OAuthUserInfo 반환")
	void getUserInfo_validId() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"id\":123456789,\"kakao_account\":{\"email\":\"user@example.com\",\"profile\":{\"nickname\":\"KakaoUser\"}}}",
						MediaType.APPLICATION_JSON
				));

		OAuthUserInfo userInfo = setup.client().getUserInfo("kakao-token");

		assertThat(userInfo.provider()).isEqualTo("kakao");
		assertThat(userInfo.providerUserId()).isEqualTo("123456789");
		setup.server().verify();
	}

	@Test
	@DisplayName("id가 없으면 SOCIAL_AUTH_FAILED")
	void getUserInfo_missingId() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"kakao_account\":{\"email\":\"user@example.com\"}}",
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> setup.client().getUserInfo("kakao-token"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);

		setup.server().verify();
	}
}
