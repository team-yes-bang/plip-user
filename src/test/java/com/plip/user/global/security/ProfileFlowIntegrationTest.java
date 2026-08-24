package com.plip.user.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.service.AuthTokenService;
import com.plip.user.domain.model.User;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileFlowIntegrationTest {

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

	@BeforeEach
	void setUpUser() {
		User user = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);

		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
		given(userAuthPersistencePort.findPrimaryEmailByUserId(1L)).willReturn(Optional.of("user@example.com"));
		given(userAuthPersistencePort.findByUserIdAndAuthType(1L, "LOCAL")).willReturn(Optional.empty());
		given(userPersistencePort.save(org.mockito.ArgumentMatchers.any(User.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	@DisplayName("Access JWT로 프로필 조회 성공")
	void getProfile_withIssuedAccessToken() throws Exception {
		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(get("/api/v1/users/me")
						.header("Authorization", "Bearer " + tokens.getAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userUuid").value(USER_UUID.toString()))
				.andExpect(jsonPath("$.nickname").value("플립이"))
				.andExpect(jsonPath("$.email").value("user@example.com"));
	}

	@Test
	@DisplayName("Access JWT로 닉네임 수정 성공")
	void updateProfileNickname_withIssuedAccessToken() throws Exception {
		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/profile")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ProfileUpdateBody("새닉네임", null))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("새닉네임"));
	}

	@Test
	@DisplayName("Access JWT로 프로필 이미지 경로 수정 성공")
	void updateProfileImagePath_withIssuedAccessToken() throws Exception {
		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/profile")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ProfileUpdateBody(null, "profiles/user.png"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.profileImagePath").value("profiles/user.png"));
	}

	private record ProfileUpdateBody(String nickname, String profileImagePath) {
	}
}
