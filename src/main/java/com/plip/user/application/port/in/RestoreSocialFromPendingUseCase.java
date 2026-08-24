package com.plip.user.application.port.in;

public interface RestoreSocialFromPendingUseCase {

	LoginResult restore(RestoreSocialFromPendingCommand command);
}
