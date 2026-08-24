package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.NotificationSettingsPatchRequest;
import com.plip.user.adapter.in.web.dto.NotificationSettingsResponse;
import com.plip.user.adapter.in.web.dto.PasswordChangeRequest;
import com.plip.user.adapter.in.web.dto.PasswordChangeResponse;
import com.plip.user.adapter.in.web.dto.ProfileUpdateRequest;
import com.plip.user.adapter.in.web.dto.UserProfileResponse;
import com.plip.user.adapter.in.web.dto.UserWithdrawResponse;
import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.GetNotificationSettingsUseCase;
import com.plip.user.application.port.in.GetUserProfileUseCase;
import com.plip.user.application.port.in.PasswordChangeCommand;
import com.plip.user.application.port.in.PasswordChangeUseCase;
import com.plip.user.application.port.in.UpdateNotificationSettingsCommand;
import com.plip.user.application.port.in.UpdateNotificationSettingsUseCase;
import com.plip.user.application.port.in.UpdateUserProfileCommand;
import com.plip.user.application.port.in.UpdateUserProfileUseCase;
import com.plip.user.application.port.in.WithdrawUserUseCase;
import com.plip.user.global.config.SwaggerTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTags.USERS_ME, description = "마이페이지 API")
@RestController
@Order(4)
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserMeController {

	private final GetUserProfileUseCase getUserProfileUseCase;
	private final UpdateUserProfileUseCase updateUserProfileUseCase;
	private final GetNotificationSettingsUseCase getNotificationSettingsUseCase;
	private final UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;
	private final PasswordChangeUseCase passwordChangeUseCase;
	private final WithdrawUserUseCase withdrawUserUseCase;

	@Operation(
			summary = "프로필 조회",
			description = "로그인한 사용자의 프로필 정보를 조회합니다. 아지트 입장 시 기본 프로필로 사용됩니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "비활성화된 계정",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "사용자 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(0)
	@GetMapping
	public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal String userUuid) {
		return ResponseEntity.ok(UserProfileResponse.of(getUserProfileUseCase.getProfile(userUuid)));
	}

	@Operation(
			summary = "프로필 수정",
			description = "닉네임·프로필 이미지 경로를 부분 수정합니다. 미전달 필드는 유지하고, "
					+ "profileImagePath에 빈 문자열을 보내면 이미지를 제거합니다. "
					+ "실제 이미지 파일 업로드는 별도 스토리지(S3/CDN)에서 수행합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "400", description = "닉네임·경로 형식 오류 또는 수정 항목 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(1)
	@PatchMapping("/profile")
	public ResponseEntity<UserProfileResponse> updateProfile(
			@AuthenticationPrincipal String userUuid,
			@Valid @RequestBody ProfileUpdateRequest request) {
		return ResponseEntity.ok(UserProfileResponse.of(updateUserProfileUseCase.updateProfile(
				UpdateUserProfileCommand.of(userUuid, request.getNickname(), request.getProfileImagePath())
		)));
	}

	@Operation(
			summary = "알림 설정 조회",
			description = "로그인 사용자의 아지트·다이어리 알림 설정을 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "사용자 또는 알림 설정 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(2)
	@GetMapping("/notification-settings")
	public ResponseEntity<NotificationSettingsResponse> getNotificationSettings(
			@AuthenticationPrincipal String userUuid) {
		return ResponseEntity.ok(NotificationSettingsResponse.of(
				getNotificationSettingsUseCase.getSettings(userUuid)
		));
	}

	@Operation(
			summary = "알림 설정 수정",
			description = "전달한 필드만 수정합니다. 토글 변경 시 해당 boolean 필드만, "
					+ "다이어리 알림 시각은 diaryNotifyTime만 전달합니다. "
					+ "저장 성공 후 user.notification-setting-updated Kafka 이벤트를 발행합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "400", description = "입력값 오류 또는 수정 항목 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "사용자 또는 알림 설정 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(3)
	@PatchMapping("/notification-settings")
	public ResponseEntity<NotificationSettingsResponse> patchNotificationSettings(
			@AuthenticationPrincipal String userUuid,
			@Valid @RequestBody NotificationSettingsPatchRequest request) {
		return ResponseEntity.ok(NotificationSettingsResponse.of(
				updateNotificationSettingsUseCase.updateSettings(UpdateNotificationSettingsCommand.of(
						userUuid,
						request.getAgitNotifyEnabled(),
						request.getDiaryNotifyEnabled(),
						request.getDiaryNotifyTime()
				))
		));
	}

	@Operation(
			summary = "비밀번호 변경",
			description = "로그인한 사용자의 비밀번호를 변경합니다. 기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다. "
					+ "변경 성공 시 Redis Refresh Token 전량 파기 후 새 Access/Refresh JWT 발급. "
					+ "기존 Access JWT는 TTL 만료까지 stateless 유효(실무 표준). 클라이언트는 응답 JWT로 교체 권장.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "변경 성공"),
			@ApiResponse(responseCode = "400", description = "기존 비밀번호와 동일",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 실패 또는 현재 비밀번호 불일치",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(4)
	@PatchMapping("/password")
	public ResponseEntity<PasswordChangeResponse> changePassword(
			@AuthenticationPrincipal String userUuid,
			@Valid @RequestBody PasswordChangeRequest request) {

		AuthTokenResult tokens = passwordChangeUseCase.changePassword(PasswordChangeCommand.of(
				userUuid,
				request.getCurrentPassword(),
				request.getNewPassword()
		));
		return ResponseEntity.ok(PasswordChangeResponse.of(tokens));
	}

	@Operation(
			summary = "회원탈퇴",
			description = "계정을 DELETED 상태로 전환하고 30일 유예 기간을 시작합니다. Redis Refresh Token 전량 파기.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "탈퇴 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "비활성화된 계정",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "사용자 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(5)
	@DeleteMapping
	public ResponseEntity<UserWithdrawResponse> withdraw(@AuthenticationPrincipal String userUuid) {
		withdrawUserUseCase.withdraw(userUuid);
		return ResponseEntity.ok(UserWithdrawResponse.of());
	}
}
