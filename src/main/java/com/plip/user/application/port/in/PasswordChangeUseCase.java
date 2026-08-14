package com.plip.user.application.port.in;

public interface PasswordChangeUseCase {

	AuthTokenResult changePassword(PasswordChangeCommand command);
}
