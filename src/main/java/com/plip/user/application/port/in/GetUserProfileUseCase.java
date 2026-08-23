package com.plip.user.application.port.in;

public interface GetUserProfileUseCase {

	UserProfileResult getProfile(String userUuid);
}
