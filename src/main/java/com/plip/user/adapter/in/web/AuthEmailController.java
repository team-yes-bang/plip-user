package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.EmailOtpRequest;
import com.plip.user.adapter.in.web.dto.EmailOtpRequestResponse;
import com.plip.user.adapter.in.web.dto.EmailOtpVerifyRequest;
import com.plip.user.adapter.in.web.dto.EmailOtpVerifyResponse;
import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.application.port.in.EmailOtpRequestCommand;
import com.plip.user.application.port.in.EmailOtpRequestUseCase;
import com.plip.user.application.port.in.EmailOtpVerifyCommand;
import com.plip.user.application.port.in.EmailOtpVerifyResult;
import com.plip.user.application.port.in.EmailOtpVerifyUseCase;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.global.config.SwaggerTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTags.AUTH_EMAIL_OTP, description = "이메일 OTP 인증 API")
@RestController
@Order(1)
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class AuthEmailController {

	private final EmailOtpRequestUseCase emailOtpRequestUseCase;
	private final EmailOtpVerifyUseCase emailOtpVerifyUseCase;

	@Operation(summary = "OTP 발송 요청", description = "입력된 이메일로 6자리 인증번호를 Gmail SMTP로 발송합니다. "
			+ "purpose=SIGNUP은 미가입 이메일만, PASSWORD_RESET은 기존 LOCAL 계정 이메일만 발송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
			@ApiResponse(responseCode = "429", description = "요청 횟수 초과",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(1)
	@PostMapping("/otp-request")
	public ResponseEntity<EmailOtpRequestResponse> requestOtp(
			@Valid @RequestBody EmailOtpRequest request,
			HttpServletRequest httpRequest) {

		EmailOtpRequestCommand command = EmailOtpRequestCommand.of(
				request.getEmail(),
				extractClientIp(httpRequest),
				resolvePurpose(request.getPurpose())
		);
		emailOtpRequestUseCase.requestOtp(command);
		return ResponseEntity.ok(EmailOtpRequestResponse.success());
	}

	@Operation(summary = "OTP 검증", description = "발송된 인증번호를 검증하고 Verification Token을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증 성공"),
			@ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(2)
	@PostMapping("/otp-verify")
	public ResponseEntity<EmailOtpVerifyResponse> verifyOtp(
			@Valid @RequestBody EmailOtpVerifyRequest request,
			HttpServletRequest httpRequest) {

		EmailOtpVerifyCommand command = EmailOtpVerifyCommand.of(
				request.getEmail(),
				request.getOtpCode(),
				extractClientIp(httpRequest),
				resolvePurpose(request.getPurpose())
		);
		EmailOtpVerifyResult result = emailOtpVerifyUseCase.verifyOtp(command);
		return ResponseEntity.ok(EmailOtpVerifyResponse.of(result.getVerificationToken()));
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private OtpPurpose resolvePurpose(OtpPurpose purpose) {
		return purpose != null ? purpose : OtpPurpose.SIGNUP;
	}
}
