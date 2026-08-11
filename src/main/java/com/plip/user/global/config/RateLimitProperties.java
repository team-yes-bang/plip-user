package com.plip.user.global.config;

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

	@Getter
	@Setter
	public static class Limit {
		private int minuteMax;
		private int dailyMax;
	}
}
