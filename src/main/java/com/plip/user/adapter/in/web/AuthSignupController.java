package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.LocalSignupRequest;
import com.plip.user.adapter.in.web.dto.LocalSignupResponse;
import com.plip.user.adapter.in.web.dto.SocialLoginRequest;
import com.plip.user.adapter.in.web.dto.SocialLoginResponse;
import com.plip.user.application.port.in.LocalSignupCommand;
import com.plip.user.application.port.in.LocalSignupCommand.TermAgreementItem;
import com.plip.user.application.port.in.LocalSignupUseCase;
import com.plip.user.application.port.in.SignupResult;
import com.plip.user.application.port.in.SocialLoginCommand;
import com.plip.user.application.port.in.SocialLoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Tag(name = "Auth - Signup & Social Login", description = "회원가입 및 소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSignupController {

	private final LocalSignupUseCase localSignupUseCase;
	private final SocialLoginUseCase socialLoginUseCase;

	@Operation(summary = "이메일 회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "인증 토큰 무효, 필수 약관 미동의, 닉네임 형식 오류",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/signup/local")
	public ResponseEntity<LocalSignupResponse> signupLocal(
			@Valid @RequestBody LocalSignupRequest request) {

		List<TermAgreementItem> termsAgreements = request.getTermsAgreements() != null
				? request.getTermsAgreements().stream()
						.map(ta -> TermAgreementItem.of(ta.getTermId(), ta.getAgreed()))
						.toList()
				: Collections.emptyList();

		LocalSignupCommand command = LocalSignupCommand.of(
				request.getEmail(),
				request.getVerificationToken(),
				request.getPassword(),
				request.getNickname(),
				termsAgreements
		);

		SignupResult result = localSignupUseCase.signup(command);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(LocalSignupResponse.of(result.getUserUuid(), result.getTokens()));
	}

	@Operation(summary = "소셜 로그인",
			description = "소셜 제공자의 액세스 토큰으로 로그인합니다. "
					+ "기존 가입 사용자(provider+providerUserId 일치)는 약관 없이 JWT를 발급하며 응답 newUser=false. "
					+ "신규 사용자는 약관 동의 후 가입·JWT 발급(newUser=true). "
					+ "닉네임·프로필 이미지는 신규 가입 시 소셜 제공자에서 자동 수집됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인/가입 성공"),
			@ApiResponse(responseCode = "401", description = "소셜 인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "422", description = "신규 사용자 — 약관 동의 필요",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/login/social/{provider}")
	public ResponseEntity<SocialLoginResponse> socialLogin(
			@PathVariable String provider,
			@Valid @RequestBody SocialLoginRequest request) {

		List<TermAgreementItem> termsAgreements = request.getTermsAgreements() != null
				? request.getTermsAgreements().stream()
						.map(ta -> TermAgreementItem.of(ta.getTermId(), ta.getAgreed()))
						.toList()
				: Collections.emptyList();

		SocialLoginCommand command = SocialLoginCommand.of(
				provider.toLowerCase(),
				request.getAccessToken(),
				termsAgreements
		);

		SignupResult result = socialLoginUseCase.login(command);
		return ResponseEntity.ok(SocialLoginResponse.of(
				result.getUserUuid(), result.isNewUser(), result.getTokens()));
	}
}
