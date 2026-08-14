package com.plip.user.application.port.in;

public interface PasswordResetUseCase {

	void resetPassword(PasswordResetCommand command);
}
