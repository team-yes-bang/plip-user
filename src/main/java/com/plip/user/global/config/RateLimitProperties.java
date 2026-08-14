package com.plip.user.global.config;

import com.plip.user.domain.model.OtpPurpose;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

	private Limit email = new Limit();
	private Limit ip = new Limit();
	private PasswordResetLimit passwordReset = new PasswordResetLimit();

	public Limit resolveEmailLimit(OtpPurpose purpose) {
		if (purpose == OtpPurpose.PASSWORD_RESET && passwordReset.getEmail() != null) {
			return passwordReset.getEmail();
		}
		return email;
	}

	public Limit resolveIpLimit(OtpPurpose purpose) {
		if (purpose == OtpPurpose.PASSWORD_RESET && passwordReset.getIp() != null) {
			return passwordReset.getIp();
		}
		return ip;
	}

	@Getter
	@Setter
	public static class Limit {
		private int minuteMax;
		private int dailyMax;
	}

	@Getter
	@Setter
	public static class PasswordResetLimit {
		private Limit email;
		private Limit ip;
	}
}
