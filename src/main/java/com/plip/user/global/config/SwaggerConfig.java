package com.plip.user.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	private static final List<Tag> ORDERED_TAGS = List.of(
			tag(SwaggerTags.AUTH_EMAIL_OTP, "이메일 OTP 인증 API"),
			tag(SwaggerTags.AUTH_TERMS, "가입·마이페이지 약관 API"),
			tag(SwaggerTags.AUTH_SIGNUP_LOGIN, "회원가입·로그인·로그아웃·비밀번호 재설정 API"),
			tag(SwaggerTags.USERS_ME, "마이페이지 API"),
			tag(SwaggerTags.AUTH_TOKEN_ACCOUNT, "JWT 토큰 재발급·탈퇴 계정 복구 API")
	);

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PLIP User Service API")
						.description("유저 서비스 API 명세서")
						.version("v1.0.0"))
				.tags(ORDERED_TAGS)
				.components(new Components()
						.addSecuritySchemes("bearerAuth", new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT Access Token")));
	}

	@Bean
	public OpenApiCustomizer tagOrderCustomizer() {
		return openApi -> openApi.setTags(ORDERED_TAGS);
	}

	private static Tag tag(String name, String description) {
		return new Tag().name(name).description(description);
	}
}
