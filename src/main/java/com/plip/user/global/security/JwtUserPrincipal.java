package com.plip.user.global.security;

/**
 * JWT 인증 성공 시 SecurityContext principal.
 */
public record JwtUserPrincipal(String userUuid) {
}
