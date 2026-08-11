package com.plip.user.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

	private int length = 6;
	private long ttlSeconds = 120;
	private long verificationTokenTtlSeconds = 600;
}
