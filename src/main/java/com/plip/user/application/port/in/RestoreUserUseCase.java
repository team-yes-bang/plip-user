package com.plip.user.application.port.in;

public interface RestoreUserUseCase {

	LoginResult restoreLocal(RestoreLocalCommand command);

	LoginResult restoreSocial(RestoreSocialCommand command);
}
