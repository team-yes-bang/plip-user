package com.plip.user.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.service.AuthTokenService;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordChangeFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AuthTokenService authTokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserPersistencePort userPersistencePort;

	@MockitoBean
	private UserAuthPersistencePort userAuthPersistencePort;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());
	private static final String CURRENT_PASSWORD = "currentPass1!";
	private static final String NEW_PASSWORD = "newPassword1!";
	private static final String ENCODED_PASSWORD = "encoded_currentPass1!";

	@BeforeEach
	void setUpUser() {
		User user = User.of(1L, USER_UUID, "테스트", null, "ACTIVE", null, null, null);
		UserAuth userAuth = UserAuth.of(
				1L, 1L, "LOCAL", "user@example.com", ENCODED_PASSWORD,
				null, null, null, null, null
		);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.of(userAuth));
	}

	@Test
	@DisplayName("가입/로그인 Access JWT로 비밀번호 변경 성공")
	void changePassword_withIssuedAccessToken() throws Exception {
		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/password")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new PasswordChangeBody(CURRENT_PASSWORD, NEW_PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists());
	}

	@Test
	@DisplayName("reissue 후 Access JWT로 비밀번호 변경 성공")
	void changePassword_withReissuedAccessToken() throws Exception {
		AuthTokenResult initialTokens = authTokenService.issueTokens(USER_UUID);
		AuthTokenResult reissuedTokens = authTokenService.reissue(initialTokens.getRefreshToken());

		mockMvc.perform(patch("/api/v1/users/me/password")
						.header("Authorization", "Bearer " + reissuedTokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new PasswordChangeBody(CURRENT_PASSWORD, NEW_PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists());
	}

	private record PasswordChangeBody(String currentPassword, String newPassword) {
	}
}
