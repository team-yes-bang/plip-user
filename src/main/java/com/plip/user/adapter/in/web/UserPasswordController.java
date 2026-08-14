package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.PasswordChangeRequest;
import com.plip.user.adapter.in.web.dto.PasswordChangeResponse;
import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.PasswordChangeCommand;
import com.plip.user.application.port.in.PasswordChangeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users - Me", description = "마이페이지 API")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserPasswordController {

	private final PasswordChangeUseCase passwordChangeUseCase;

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
}
