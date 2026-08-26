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

class NaverOAuthClientTest {

	private static final String USERINFO_URL = "https://openapi.naver.com/v1/nid/me";

	private MockRestSetup createClient() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl(USERINFO_URL).build();
		return new MockRestSetup(server, new NaverOAuthClient(restClient));
	}

	private record MockRestSetup(MockRestServiceServer server, NaverOAuthClient client) {
	}

	@Test
	@DisplayName("id가 있으면 OAuthUserInfo 반환")
	void getUserInfo_validId() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"response\":{\"id\":\"naver-id-123\",\"email\":\"user@example.com\",\"nickname\":\"NaverUser\"}}",
						MediaType.APPLICATION_JSON
				));

		OAuthUserInfo userInfo = setup.client().getUserInfo("naver-token");

		assertThat(userInfo.provider()).isEqualTo("naver");
		assertThat(userInfo.providerUserId()).isEqualTo("naver-id-123");
		setup.server().verify();
	}

	@Test
	@DisplayName("id가 없으면 SOCIAL_AUTH_FAILED")
	void getUserInfo_missingId() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"response\":{\"email\":\"user@example.com\",\"nickname\":\"NaverUser\"}}",
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> setup.client().getUserInfo("naver-token"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);

		setup.server().verify();
	}

	@Test
	@DisplayName("id가 blank이면 SOCIAL_AUTH_FAILED")
	void getUserInfo_blankId() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"response\":{\"id\":\"   \",\"email\":\"user@example.com\"}}",
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> setup.client().getUserInfo("naver-token"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);

		setup.server().verify();
	}
}
