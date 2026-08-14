package com.plip.user.adapter.out.security;

import com.plip.user.application.port.out.TokenProviderPort;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.config.JwtProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProviderPort {

	private static final String CLAIM_USER_UUID = "user_uuid";
	private static final String CLAIM_TOKEN_TYPE = "tokenType";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";
	private static final String FALLBACK_SECRET =
			"user-service-default-jwt-secret-key-change-me-please";

	private final JwtProperties properties;

	@Override
	public String createAccessToken(UuidV7 userUuid) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + properties.getAccessTokenMinutes() * 60_000L);
		return Jwts.builder()
				.subject(userUuid.toString())
				.claim(CLAIM_USER_UUID, userUuid.toString())
				.claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
				.issuedAt(now)
				.expiration(exp)
				.signWith(signingKey())
				.compact();
	}

	@Override
	public String createRefreshToken(UuidV7 userUuid) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + properties.getRefreshTokenDays() * 24 * 60 * 60_000L);
		return Jwts.builder()
				.subject(userUuid.toString())
				.claim(CLAIM_USER_UUID, userUuid.toString())
				.claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
				.issuedAt(now)
				.expiration(exp)
				.signWith(signingKey())
				.compact();
	}

	@Override
	public UuidV7 parseAccessToken(String token) {
		return parseToken(token, TOKEN_TYPE_ACCESS);
	}

	@Override
	public UuidV7 parseRefreshToken(String token) {
		return parseToken(token, TOKEN_TYPE_REFRESH);
	}

	private UuidV7 parseToken(String token, String expectedType) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(signingKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();

			String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
			if (!expectedType.equals(tokenType)) {
				throw new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID);
			}

			String userUuid = claims.get(CLAIM_USER_UUID, String.class);
			if (userUuid == null || userUuid.isBlank()) {
				throw new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID);
			}
			return UuidV7.parse(userUuid);
		} catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
			if (TOKEN_TYPE_REFRESH.equals(expectedType)) {
				throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
			}
			throw new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID);
		}
	}

	private SecretKey signingKey() {
		String raw = properties.getSecret();
		if (raw == null || raw.isBlank()) {
			raw = FALLBACK_SECRET;
		}
		byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			bytes = sha256(raw);
		}
		return Keys.hmacShaKeyFor(bytes);
	}

	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
