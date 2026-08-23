package com.plip.user.application.port.in;

public interface UpdateUserProfileUseCase {

	UserProfileResult updateProfile(UpdateUserProfileCommand command);
}
