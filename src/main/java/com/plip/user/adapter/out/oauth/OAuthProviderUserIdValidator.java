package com.plip.user.adapter.out.oauth;

import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;

final class OAuthProviderUserIdValidator {

	private OAuthProviderUserIdValidator() {
	}

	static String requireProviderUserId(String providerUserId) {
		if (providerUserId == null || providerUserId.isBlank()) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		String normalized = providerUserId.trim();
		if ("null".equalsIgnoreCase(normalized)) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		return normalized;
	}

	static String requireProviderUserId(Object rawId) {
		if (rawId == null) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		return requireProviderUserId(String.valueOf(rawId));
	}
}
