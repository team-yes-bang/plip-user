package com.plip.user.application.service;

import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class LocalSignupEmailValidatorTest {

	private static final String EMAIL = "test@example.com";

	@InjectMocks
	private LocalSignupEmailValidator validator;

	@Mock
	private UserAuthPersistencePort userAuthPersistencePort;

	@Mock
	private UserPersistencePort userPersistencePort;

	@Test
	@DisplayName("가입 이력이 없으면 OTP·가입 허용")
	void allowWhenEmailNotRegistered() {
		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.empty());

		assertThatCode(() -> validator.assertEligibleForSignup(EMAIL)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("활성 계정 이메일이면 EMAIL_ALREADY_REGISTERED")
	void rejectActiveAccount() {
		UserAuth userAuth = UserAuth.of(1L, 10L, "LOCAL", EMAIL, "hash", null, null, null, null, null);
		User user = User.of(10L, UuidV7.of(UUID.randomUUID()), "닉네임", null, "ACTIVE", null, null, null);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(10L)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> validator.assertEligibleForSignup(EMAIL))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);
	}

	@Test
	@DisplayName("탈퇴 유예 기간 중이면 USER_DELETED_RESTORABLE")
	void rejectWithdrawnWithinGracePeriod() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(10);
		UserAuth userAuth = UserAuth.of(1L, 10L, "LOCAL", EMAIL, "hash", null, null, null, null, null);
		User user = User.of(10L, UuidV7.of(UUID.randomUUID()), "닉네임", null, "DELETED", null, null, deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(10L)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> validator.assertEligibleForSignup(EMAIL))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.USER_DELETED_RESTORABLE);
	}

	@Test
	@DisplayName("탈퇴 유예 기간 만료 후에는 OTP·가입 허용")
	void allowWithdrawnAfterGracePeriod() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
		UserAuth userAuth = UserAuth.of(1L, 10L, "LOCAL", EMAIL, "hash", null, null, null, null, null);
		User user = User.of(10L, UuidV7.of(UUID.randomUUID()), "닉네임", null, "DELETED", null, null, deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(10L)).willReturn(Optional.of(user));

		assertThatCode(() -> validator.assertEligibleForSignup(EMAIL)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("탈퇴 유예 기간 만료 시 기존 LOCAL auth soft delete")
	void releaseExpiredWithdrawnEmail() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
		UserAuth userAuth = UserAuth.of(1L, 10L, "LOCAL", EMAIL, "hash", null, null, null, null, null);
		User user = User.of(10L, UuidV7.of(UUID.randomUUID()), "닉네임", null, "DELETED", null, null, deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(10L)).willReturn(Optional.of(user));

		validator.releaseExpiredWithdrawnEmail(EMAIL);

		then(userAuthPersistencePort).should().softDelete(eq(1L), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("탈퇴 유예 기간 중에는 auth soft delete 하지 않음")
	void doNotReleaseWithinGracePeriod() {
		LocalDateTime deletedAt = LocalDateTime.now().minusDays(10);
		UserAuth userAuth = UserAuth.of(1L, 10L, "LOCAL", EMAIL, "hash", null, null, null, null, null);
		User user = User.of(10L, UuidV7.of(UUID.randomUUID()), "닉네임", null, "DELETED", null, null, deletedAt);

		given(userAuthPersistencePort.findByEmailAndAuthType(EMAIL, "LOCAL")).willReturn(Optional.of(userAuth));
		given(userPersistencePort.findById(10L)).willReturn(Optional.of(user));

		validator.releaseExpiredWithdrawnEmail(EMAIL);

		then(userAuthPersistencePort).should(never()).softDelete(any(), any());
	}
}
