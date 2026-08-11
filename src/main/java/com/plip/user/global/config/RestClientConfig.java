package com.plip.user.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient resendRestClient(ResendProperties resendProperties) {
		return RestClient.builder()
				.baseUrl("https://api.resend.com")
				.defaultHeader("Authorization", "Bearer " + resendProperties.getApiKey())
				.defaultHeader("Content-Type", "application/json")
				.build();
	}
}
