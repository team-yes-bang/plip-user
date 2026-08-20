package com.plip.user.adapter.in.web;

import com.plip.user.adapter.in.web.dto.TermsListResponse;
import com.plip.user.application.port.in.GetActiveTermsUseCase;
import com.plip.user.global.config.SwaggerTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTags.AUTH_TERMS, description = "가입·마이페이지 약관 API")
@RestController
@Order(2)
@RequestMapping("/api/v1/auth/terms")
@RequiredArgsConstructor
public class AuthTermsController {

	private final GetActiveTermsUseCase getActiveTermsUseCase;

	@Operation(summary = "ACTIVE 약관 목록", description = "가입·소셜 약관 동의 UI용 ACTIVE 약관 목록. term_code당 version(major.minor) 최신 ACTIVE 1건만 반환.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@Order(1)
	@GetMapping
	public ResponseEntity<TermsListResponse> getActiveTerms() {
		return ResponseEntity.ok(TermsListResponse.of(getActiveTermsUseCase.getActiveTerms()));
	}
}
