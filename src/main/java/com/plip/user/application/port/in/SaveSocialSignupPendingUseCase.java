package com.plip.user.application.port.in;

public interface SaveSocialSignupPendingUseCase {

	SocialSignupPendingResult savePending(SaveSocialSignupPendingCommand command);
}
