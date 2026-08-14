package com.plip.user.application.port.in;

public interface LocalLoginUseCase {

	LoginResult login(LocalLoginCommand command);
}
