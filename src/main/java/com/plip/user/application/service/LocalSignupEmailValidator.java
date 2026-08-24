package com.plip.user.application.service;

import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocalSignupEmailValidator {

	private static final String AUTH_TYPE_LOCAL = "LOCAL";

	private final UserAuthPersistencePort userAuthPersistencePort;
	private final UserPersistencePort userPersistencePort;

	public void assertEligibleForSignup(String email) {
		resolveConflict(email).ifPresent(exception -> {
			throw exception;
		});
	}

	public void releaseExpiredWithdrawnEmail(String email) {
		userAuthPersistencePort.findByEmailAndAuthType(email, AUTH_TYPE_LOCAL)
				.ifPresent(userAuth -> {
					User user = userPersistencePort.findById(userAuth.getUserId()).orElse(null);
					if (user == null) {
						return;
					}
					if (user.isDeleted() && !user.isWithinWithdrawalGracePeriod(LocalDateTime.now())) {
						userAuthPersistencePort.softDelete(userAuth.getId(), LocalDateTime.now());
					}
				});
	}

	private java.util.Optional<BusinessException> resolveConflict(String email) {
		java.util.Optional<UserAuth> userAuth = userAuthPersistencePort.findByEmailAndAuthType(email, AUTH_TYPE_LOCAL);
		if (userAuth.isEmpty()) {
			return java.util.Optional.empty();
		}

		User user = userPersistencePort.findById(userAuth.get().getUserId()).orElse(null);
		if (user == null) {
			return java.util.Optional.empty();
		}

		if (user.isDeleted()) {
			if (user.isWithinWithdrawalGracePeriod(LocalDateTime.now())) {
				return java.util.Optional.of(new BusinessException(ErrorCode.USER_DELETED_RESTORABLE));
			}
			return java.util.Optional.empty();
		}

		return java.util.Optional.of(new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED));
	}
}
