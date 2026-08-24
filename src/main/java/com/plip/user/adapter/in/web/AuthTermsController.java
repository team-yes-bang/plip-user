package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.adapter.in.web.dto.TermsAgreementsUpdateRequest;
import com.plip.user.adapter.in.web.dto.TermsAgreementsUpdateResponse;
import com.plip.user.adapter.in.web.dto.TermsListResponse;
import com.plip.user.application.port.in.GetActiveTermsUseCase;
import com.plip.user.application.port.in.UpdateTermsAgreementsCommand;
import com.plip.user.application.port.in.UpdateTermsAgreementsUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTags.AUTH_TERMS, description = "가입·마이페이지 약관 API")
@RestController
@Order(2)
@RequestMapping("/api/v1/auth/terms")
@RequiredArgsConstructor
public class AuthTermsController {

	private final GetActiveTermsUseCase getActiveTermsUseCase;
	private final UpdateTermsAgreementsUseCase updateTermsAgreementsUseCase;

	@Operation(summary = "ACTIVE 약관 목록", description = "가입·소셜 약관 동의 UI용 ACTIVE 약관 목록. term_code당 version(major.minor) 최신 ACTIVE 1건만 반환.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@Order(1)
	@GetMapping
	public ResponseEntity<TermsListResponse> getActiveTerms() {
		return ResponseEntity.ok(TermsListResponse.of(getActiveTermsUseCase.getActiveTerms()));
	}

	@Operation(
			summary = "선택 약관 동의·철회",
			description = "로그인 사용자의 선택(비필수) 약관 동의 상태를 변경합니다. 필수 약관 철회는 거부됩니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "변경 성공"),
			@ApiResponse(responseCode = "400", description = "필수 약관 철회·약관 없음·항목 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "사용자 없음",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@Order(2)
	@PatchMapping("/agreements")
	public ResponseEntity<TermsAgreementsUpdateResponse> updateAgreements(
			@AuthenticationPrincipal String userUuid,
			@Valid @RequestBody TermsAgreementsUpdateRequest request) {
		return ResponseEntity.ok(TermsAgreementsUpdateResponse.of(
				updateTermsAgreementsUseCase.updateAgreements(UpdateTermsAgreementsCommand.of(
						userUuid,
						request.getAgreements().stream()
								.map(item -> UpdateTermsAgreementsCommand.TermAgreementItem.of(
										item.getTermId(), item.getAgreed()))
								.toList()
				))
		));
	}
}
