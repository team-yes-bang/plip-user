package com.plip.user.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.adapter.in.web.dto.ErrorResponse;
import com.plip.user.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		response.setStatus(ErrorCode.ACCESS_TOKEN_INVALID.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(
				response.getWriter(),
				ErrorResponse.of(
						ErrorCode.ACCESS_TOKEN_INVALID.getCode(),
						ErrorCode.ACCESS_TOKEN_INVALID.getMessage()
				)
		);
	}
}
