package com.plip.user.adapter.out.oauth;

import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthProviderUserIdValidatorTest {

	@Test
	@DisplayName("유효한 providerUserId 반환")
	void requireProviderUserId_valid() {
		assertThat(OAuthProviderUserIdValidator.requireProviderUserId("kakao-123")).isEqualTo("kakao-123");
	}

	@Test
	@DisplayName("null이면 SOCIAL_AUTH_FAILED")
	void requireProviderUserId_nullObject() {
		assertThatThrownBy(() -> OAuthProviderUserIdValidator.requireProviderUserId((Object) null))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);
	}

	@Test
	@DisplayName("문자열 null이면 SOCIAL_AUTH_FAILED")
	void requireProviderUserId_nullLiteral() {
		assertThatThrownBy(() -> OAuthProviderUserIdValidator.requireProviderUserId("null"))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED);
	}
}
