package com.plip.user.application.port.in;

public interface CompleteSocialSignupUseCase {

	SignupResult complete(CompleteSocialSignupCommand command);
}
