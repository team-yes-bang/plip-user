package com.plip.user.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.application.service.AuthTokenService;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserNotificationSetting;
import com.plip.user.domain.model.UserTermsAgreement;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TermsNotificationFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AuthTokenService authTokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserPersistencePort userPersistencePort;

	@MockitoBean
	private TermPersistencePort termPersistencePort;

	@MockitoBean
	private UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;

	@MockitoBean
	private UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;

	private static final UuidV7 USER_UUID = UuidV7.of(UUID.randomUUID());

	@BeforeEach
	void setUp() {
		User user = User.of(1L, USER_UUID, "플립이", null, "ACTIVE",
				LocalDateTime.now(), LocalDateTime.now(), null);
		given(userPersistencePort.findByUserUuid(USER_UUID)).willReturn(Optional.of(user));
	}

	@Test
	@DisplayName("Access JWT로 유저 약관 동의 상태 GET 성공")
	void getTermsAgreements_withIssuedAccessToken() throws Exception {
		Term marketing = Term.of(3L, null, "마케팅", "terms/marketing", "MARKETING", "1.0",
				false, "ACTIVE", null, null);

		given(termPersistencePort.findAllActive()).willReturn(List.of(marketing));
		given(userTermsAgreementPersistencePort.findAllByUserId(1L)).willReturn(List.of());

		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(get("/api/v1/users/me/terms-agreements")
						.header("Authorization", "Bearer " + tokens.getAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agreements[0].termId").value(3))
				.andExpect(jsonPath("$.agreements[0].termCode").value("MARKETING"))
				.andExpect(jsonPath("$.agreements[0].agreed").value(false));
	}

	@Test
	@DisplayName("Access JWT로 선택 약관 동의 PATCH 성공")
	void updateTermsAgreements_withIssuedAccessToken() throws Exception {
		Term marketing = Term.of(3L, null, "마케팅", "terms/marketing", "MARKETING", "1.0",
				false, "ACTIVE", null, null);

		given(termPersistencePort.findAllByIdIn(List.of(3L))).willReturn(List.of(marketing));
		given(userTermsAgreementPersistencePort.findByUserIdAndTermId(1L, 3L)).willReturn(Optional.empty());
		given(userTermsAgreementPersistencePort.save(any(UserTermsAgreement.class)))
				.willAnswer(invocation -> {
					UserTermsAgreement agreement = invocation.getArgument(0);
					return UserTermsAgreement.of(
							10L, agreement.getUserId(), agreement.getTermId(), agreement.isAgreed(),
							agreement.getAgreedAt(), agreement.getRevokedAt(), null, null
					);
				});

		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/terms-agreements")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"agreements":[{"termId":3,"agreed":true}]}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agreements[0].termId").value(3))
				.andExpect(jsonPath("$.agreements[0].agreed").value(true));
	}

	@Test
	@DisplayName("Access JWT로 알림 설정 GET 성공")
	void getNotificationSettings_withIssuedAccessToken() throws Exception {
		UserNotificationSetting setting = UserNotificationSetting.of(
				10L, 1L, true, true, LocalTime.of(21, 0), null, null
		);

		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.of(setting));

		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(get("/api/v1/users/me/notification-settings")
						.header("Authorization", "Bearer " + tokens.getAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agitNotifyEnabled").value(true))
				.andExpect(jsonPath("$.diaryNotifyTime").value("21:00:00"));
	}

	@Test
	@DisplayName("Access JWT로 알림 토글 partial PATCH 성공")
	void patchNotificationSettings_toggleOnly() throws Exception {
		UserNotificationSetting setting = UserNotificationSetting.of(
				10L, 1L, true, true, LocalTime.of(21, 0), null, null
		);

		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.of(setting));
		given(userNotificationSettingPersistencePort.save(any(UserNotificationSetting.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/notification-settings")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"agitNotifyEnabled\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agitNotifyEnabled").value(false))
				.andExpect(jsonPath("$.diaryNotifyEnabled").value(true))
				.andExpect(jsonPath("$.diaryNotifyTime").value("21:00:00"));
	}

	@Test
	@DisplayName("Access JWT로 다이어리 알림 시각 partial PATCH 성공")
	void patchNotificationSettings_diaryTimeOnly() throws Exception {
		UserNotificationSetting setting = UserNotificationSetting.of(
				10L, 1L, true, true, LocalTime.of(21, 0), null, null
		);

		given(userNotificationSettingPersistencePort.findByUserId(1L)).willReturn(Optional.of(setting));
		given(userNotificationSettingPersistencePort.save(any(UserNotificationSetting.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		AuthTokenResult tokens = authTokenService.issueTokens(USER_UUID);

		mockMvc.perform(patch("/api/v1/users/me/notification-settings")
						.header("Authorization", "Bearer " + tokens.getAccessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"diaryNotifyTime\":\"09:30:00\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.diaryNotifyTime").value("09:30:00"));
	}
}
