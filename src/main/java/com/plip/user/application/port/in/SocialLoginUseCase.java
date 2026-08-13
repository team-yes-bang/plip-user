package com.plip.user.application.port.in;

public interface SocialLoginUseCase {

	SignupResult login(SocialLoginCommand command);
}
