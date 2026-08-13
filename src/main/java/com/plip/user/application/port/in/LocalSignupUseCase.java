package com.plip.user.application.port.in;

public interface LocalSignupUseCase {

	SignupResult signup(LocalSignupCommand command);
}
