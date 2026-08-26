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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleOAuthClientTest {

	private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

	private MockRestSetup createClient() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl(USERINFO_URL).build();
		return new MockRestSetup(server, new GoogleOAuthClient(restClient));
	}

	private record MockRestSetup(MockRestServiceServer server, GoogleOAuthClient client) {
	}

	@Test
	@DisplayName("sub가 있으면 OAuthUserInfo 반환")
	void getUserInfo_validSub() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andExpect(header("Authorization", "Bearer google-token"))
				.andRespond(withSuccess(
						"{\"sub\":\"google-sub-123\",\"email\":\"user@example.com\",\"name\":\"User\"}",
						MediaType.APPLICATION_JSON
				));

		OAuthUserInfo userInfo = setup.client().getUserInfo("google-token");

		assertThat(userInfo.provider()).isEqualTo("google");
		assertThat(userInfo.providerUserId()).isEqualTo("google-sub-123");
		assertThat(userInfo.email()).isEqualTo("user@example.com");
		setup.server().verify();
	}

	@Test
	@DisplayName("sub가 없으면 SOCIAL_AUTH_FAILED")
	void getUserInfo_missingSub() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"email\":\"user@example.com\",\"name\":\"User\"}",
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> setup.client().getUserInfo("google-token"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);

		setup.server().verify();
	}

	@Test
	@DisplayName("sub가 blank이면 SOCIAL_AUTH_FAILED")
	void getUserInfo_blankSub() {
		MockRestSetup setup = createClient();

		setup.server().expect(requestTo(USERINFO_URL))
				.andRespond(withSuccess(
						"{\"sub\":\"   \",\"email\":\"user@example.com\"}",
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> setup.client().getUserInfo("google-token"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);

		setup.server().verify();
	}
}
