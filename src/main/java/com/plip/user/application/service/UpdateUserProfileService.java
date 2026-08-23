package com.plip.user.application.service;

import com.plip.user.application.port.in.UpdateUserProfileCommand;
import com.plip.user.application.port.in.UpdateUserProfileUseCase;
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
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

	private static final int PROFILE_IMAGE_PATH_MAX_LENGTH = 255;

	private final UserPersistencePort userPersistencePort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserAccountStatusValidator userAccountStatusValidator;

	@Override
	@Transactional
	public UserProfileResult updateProfile(UpdateUserProfileCommand command) {
		if (command.isEmpty()) {
			throw new BusinessException(ErrorCode.PROFILE_UPDATE_EMPTY);
		}

		UuidV7 userUuid = UuidV7.parse(command.getUserUuid());
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userAccountStatusValidator.validateLoginEligible(user);

		if (command.hasNickname()) {
			validateNickname(command.getNickname());
		}
		if (command.hasProfileImagePath()) {
			validateProfileImagePath(command.getProfileImagePath());
		}

		user.updateProfile(
				command.hasNickname() ? command.getNickname().trim() : null,
				command.hasProfileImagePath() ? command.getProfileImagePath() : null
		);

		User savedUser = userPersistencePort.save(user);
		String email = userAuthPersistencePort.findPrimaryEmailByUserId(savedUser.getId()).orElse("");

		return UserProfileResult.of(
				savedUser.getUserUuid().toString(),
				savedUser.getNickname(),
				savedUser.getProfileImagePath(),
				email
		);
	}

	private void validateNickname(String nickname) {
		if (nickname == null || nickname.trim().length() < 2 || nickname.trim().length() > 12) {
			throw new BusinessException(ErrorCode.INVALID_NICKNAME);
		}
	}

	private void validateProfileImagePath(String profileImagePath) {
		if (profileImagePath != null && profileImagePath.trim().length() > PROFILE_IMAGE_PATH_MAX_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "profileImagePath: 255자 이하여야 합니다.");
		}
	}
}
