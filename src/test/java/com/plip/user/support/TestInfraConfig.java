package com.plip.user.support;

import com.plip.user.application.port.out.EmailSendPort;
import com.plip.user.application.port.out.EventPublisherPort;
import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.application.port.out.OtpPort;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.RateLimitPort;
import com.plip.user.application.port.out.VerificationTokenPort;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("test")
public class TestInfraConfig {

	@Bean
	public StringRedisTemplate stringRedisTemplate() {
		return Mockito.mock(StringRedisTemplate.class);
	}

	@Bean
	@Primary
	public OtpPort otpPort() {
		return new OtpPort() {
			@Override
			public void save(String email, String otpCode, long ttlSeconds) {}

			@Override
			public String findByEmail(String email) { return null; }

			@Override
			public void deleteByEmail(String email) {}
		};
	}

	@Bean
	@Primary
	public VerificationTokenPort verificationTokenPort() {
		return new VerificationTokenPort() {
			@Override
			public void save(String email, String token, long ttlSeconds) {}

			@Override
			public String findByEmail(String email) { return null; }

			@Override
			public void deleteByEmail(String email) {}
		};
	}

	@Bean
	@Primary
	public RateLimitPort rateLimitPort() {
		return new RateLimitPort() {
			@Override
			public boolean isExceeded(String key, int maxCount, long windowSeconds) { return false; }
		};
	}

	@Bean
	@Primary
	public EmailSendPort emailSendPort() {
		return (toEmail, otpCode) -> {};
	}

	@Bean
	@Primary
	public EventPublisherPort eventPublisherPort() {
		return (topic, key, payload) -> {};
	}

	@Bean
	@Primary
	public PasswordEncoderPort passwordEncoderPort() {
		return new PasswordEncoderPort() {
			@Override
			public String encode(String rawPassword) { return "encoded_" + rawPassword; }

			@Override
			public boolean matches(String rawPassword, String encodedPassword) {
				return encodedPassword.equals("encoded_" + rawPassword);
			}
		};
	}

	@Bean
	@Primary
	public OAuthUserInfoPort oAuthUserInfoPort() {
		return (provider, accessToken) -> new OAuthUserInfoPort.OAuthUserInfo(
				provider, "test-provider-id", "social@example.com", "SocialUser", null
		);
	}
}
