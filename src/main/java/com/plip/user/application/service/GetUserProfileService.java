package com.plip.user.application.service;

import com.plip.user.application.port.in.GetUserProfileUseCase;
import com.plip.user.application.port.in.UserProfileResult;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserProfileService implements GetUserProfileUseCase {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserPersistencePort userPersistencePort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	public UserProfileResult getProfile(String userUuidValue) {
		UuidV7 userUuid = UuidV7.parse(userUuidValue);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		String email = userAuthPersistencePort.findPrimaryEmailByUserId(user.getId()).orElse("");
		boolean hasLocalAuth = userAuthPersistencePort
				.findByUserIdAndAuthType(user.getId(), AUTH_TYPE_LOCAL)
				.isPresent();

		return UserProfileResult.of(
				user.getUserUuid().toString(),
				user.getNickname(),
				user.getProfileImagePath(),
				email,
				hasLocalAuth
		);
	}
}
