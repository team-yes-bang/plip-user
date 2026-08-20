package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.AccountRestoreResponse;
import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.LocalLoginRequest;
import com.plip.user.adapter.in.web.dto.LocalLoginResponse;
import com.plip.user.adapter.in.web.dto.LogoutRequest;
import com.plip.user.adapter.in.web.dto.LogoutResponse;
import com.plip.user.adapter.in.web.dto.PasswordResetRequest;
import com.plip.user.adapter.in.web.dto.PasswordResetResponse;
import com.plip.user.adapter.in.web.dto.SocialRestoreRequest;
import com.plip.user.adapter.in.web.dto.TokenReissueRequest;
import com.plip.user.adapter.in.web.dto.TokenReissueResponse;
import com.plip.user.application.port.in.LocalLoginCommand;
import com.plip.user.application.port.in.LocalLoginUseCase;
import com.plip.user.application.port.in.LoginResult;
import com.plip.user.application.port.in.LogoutUseCase;
import com.plip.user.application.port.in.PasswordResetCommand;
import com.plip.user.application.port.in.PasswordResetUseCase;
import com.plip.user.application.port.in.RestoreLocalCommand;
import com.plip.user.application.port.in.RestoreSocialCommand;
import com.plip.user.application.port.in.RestoreUserUseCase;
import com.plip.user.application.port.in.TokenReissueUseCase;
import com.plip.user.global.config.SwaggerTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Order(5)
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthLoginController {

	private final LocalLoginUseCase localLoginUseCase;
	private final TokenReissueUseCase tokenReissueUseCase;
	private final PasswordResetUseCase passwordResetUseCase;
	private final LogoutUseCase logoutUseCase;
	private final RestoreUserUseCase restoreUserUseCase;

	@Operation(summary = "로컬 로그인", description = "이메일과 비밀번호로 로그인하고 JWT를 발급합니다.", tags = {
			SwaggerTags.AUTH_SIGNUP_LOGIN })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "비활성화된 계정",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(2)
	@PostMapping("/login/local")
	public ResponseEntity<LocalLoginResponse> loginLocal(@Valid @RequestBody LocalLoginRequest request) {
		LoginResult result = localLoginUseCase.login(
				LocalLoginCommand.of(request.getEmail(), request.getPassword()));
		return ResponseEntity.ok(LocalLoginResponse.of(result.getUserUuid(), result.getTokens()));
	}

	@Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스·리프레시 토큰을 재발급합니다 (RTR).", tags = {
			SwaggerTags.AUTH_TOKEN_ACCOUNT })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "재발급 성공"),
			@ApiResponse(responseCode = "401", description = "리프레시 토큰 무효",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "비활성화된 계정",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(1)
	@PostMapping("/reissue")
	public ResponseEntity<TokenReissueResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
		return ResponseEntity.ok(TokenReissueResponse.of(tokenReissueUseCase.reissue(request.getRefreshToken())));
	}

	@Operation(summary = "비밀번호 재설정", description = "이메일 OTP 인증 후 비밀번호를 재설정합니다.", tags = {
			SwaggerTags.AUTH_SIGNUP_LOGIN })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "재설정 성공"),
			@ApiResponse(responseCode = "400", description = "인증 토큰 무효, 기존 비밀번호와 동일",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "비활성화된 계정",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(4)
	@PostMapping("/password-reset")
	public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
		passwordResetUseCase.resetPassword(PasswordResetCommand.of(
				request.getEmail(),
				request.getVerificationToken(),
				request.getNewPassword()
		));
		return ResponseEntity.ok(PasswordResetResponse.of());
	}

	@Operation(
			summary = "로그아웃",
			description = "리프레시 토큰을 무효화하고 user.logout Kafka 이벤트를 발행합니다. "
					+ "액세스 토큰은 TTL 만료까지 stateless 유효.",
			tags = { SwaggerTags.AUTH_SIGNUP_LOGIN }
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
			@ApiResponse(responseCode = "401", description = "리프레시 토큰 무효",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(5)
	@PostMapping("/logout")
	public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
		logoutUseCase.logout(request.getRefreshToken());
		return ResponseEntity.ok(LogoutResponse.of());
	}

	@Operation(
			summary = "로컬 계정 복구",
			description = "탈퇴 유예 기간(30일) 내 DELETED 계정을 이메일·비밀번호로 복구하고 JWT를 재발급합니다. "
					+ "로그인 시 AUTH_010 응답 후 본 API 호출. JWT 불필요(public).",
			tags = { SwaggerTags.AUTH_TOKEN_ACCOUNT }
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "복구 성공"),
			@ApiResponse(responseCode = "400", description = "복구 대상 아님",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "유예 기간 만료",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(2)
	@PostMapping("/restore/local")
	public ResponseEntity<AccountRestoreResponse> restoreLocal(@Valid @RequestBody LocalLoginRequest request) {
		LoginResult result = restoreUserUseCase.restoreLocal(
				RestoreLocalCommand.of(request.getEmail(), request.getPassword()));
		return ResponseEntity.ok(AccountRestoreResponse.of(result.getUserUuid(), result.getTokens()));
	}

	@Operation(
			summary = "소셜 계정 복구",
			description = "탈퇴 유예 기간(30일) 내 DELETED 계정을 소셜 액세스 토큰으로 복구하고 JWT를 재발급합니다. "
					+ "소셜 로그인 시 AUTH_010 응답 후 본 API 호출. JWT 불필요(public).",
			tags = { SwaggerTags.AUTH_TOKEN_ACCOUNT }
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "복구 성공"),
			@ApiResponse(responseCode = "400", description = "복구 대상 아님",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "소셜 인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "유예 기간 만료",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(3)
	@PostMapping("/restore/social/{provider}")
	public ResponseEntity<AccountRestoreResponse> restoreSocial(
			@PathVariable String provider,
			@Valid @RequestBody SocialRestoreRequest request) {
		LoginResult result = restoreUserUseCase.restoreSocial(
				RestoreSocialCommand.of(provider, request.getAccessToken()));
		return ResponseEntity.ok(AccountRestoreResponse.of(result.getUserUuid(), result.getTokens()));
	}
}
