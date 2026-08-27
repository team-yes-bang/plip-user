package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.NotificationInboxResponse;
import com.plip.user.adapter.in.web.dto.NotificationItemResponse;
import com.plip.user.adapter.in.web.dto.NotificationReadAllResponse;
import com.plip.user.adapter.in.web.dto.NotificationUnreadCountResponse;
import com.plip.user.application.port.in.GetNotificationsUseCase;
import com.plip.user.application.port.in.GetUnreadNotificationCountUseCase;
import com.plip.user.application.port.in.MarkAllNotificationsReadUseCase;
import com.plip.user.application.port.in.MarkNotificationReadUseCase;
import com.plip.user.application.port.in.SeedNotificationsUseCase;
import com.plip.user.global.config.SwaggerTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTags.USERS_ME, description = "마이페이지 API")
@RestController
@Order(6)
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
public class UserMeNotificationsController {

	private final GetNotificationsUseCase getNotificationsUseCase;
	private final GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;
	private final MarkNotificationReadUseCase markNotificationReadUseCase;
	private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
	private final SeedNotificationsUseCase seedNotificationsUseCase;

	@Operation(summary = "알림함 조회", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping
	public ResponseEntity<NotificationInboxResponse> list(
			@AuthenticationPrincipal String userUuid,
			@RequestParam(name = "limit", required = false, defaultValue = "30") int limit
	) {
		return ResponseEntity.ok(NotificationInboxResponse.of(getNotificationsUseCase.getInbox(userUuid, limit)));
	}

	@Operation(summary = "읽지 않은 알림 수", security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping("/unread-count")
	public ResponseEntity<NotificationUnreadCountResponse> unreadCount(
			@AuthenticationPrincipal String userUuid
	) {
		return ResponseEntity.ok(NotificationUnreadCountResponse.of(
				getUnreadNotificationCountUseCase.getUnreadCount(userUuid)
		));
	}

	@Operation(summary = "알림 읽음", security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<NotificationItemResponse> markRead(
			@AuthenticationPrincipal String userUuid,
			@PathVariable("notificationId") Long notificationId
	) {
		return ResponseEntity.ok(NotificationItemResponse.of(
				markNotificationReadUseCase.markRead(userUuid, notificationId)
		));
	}

	@Operation(summary = "모두 읽음", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/read-all")
	public ResponseEntity<NotificationReadAllResponse> markAllRead(
			@AuthenticationPrincipal String userUuid
	) {
		return ResponseEntity.ok(NotificationReadAllResponse.of(markAllNotificationsReadUseCase.markAllRead(userUuid)));
	}

	@Operation(summary = "데모 알림 시드", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/seed")
	public ResponseEntity<NotificationInboxResponse> seed(@AuthenticationPrincipal String userUuid) {
		return ResponseEntity.ok(NotificationInboxResponse.of(seedNotificationsUseCase.seed(userUuid)));
	}
}
