package com.plip.user.application.service;

import com.plip.user.application.port.in.AuthTokenResult;
import com.plip.user.application.port.in.LocalSignupCommand;
import com.plip.user.application.port.in.LocalSignupCommand.TermAgreementItem;
import com.plip.user.application.port.in.LocalSignupUseCase;
import com.plip.user.application.port.in.SignupResult;
import com.plip.user.application.port.in.SocialLoginCommand;
import com.plip.user.application.port.in.SocialLoginUseCase;
import com.plip.user.application.port.out.EventPublisherPort;
import com.plip.user.application.port.out.OAuthUserInfoPort;
import com.plip.user.application.port.out.OAuthUserInfoPort.OAuthUserInfo;
import com.plip.user.application.port.out.PasswordEncoderPort;
import com.plip.user.application.port.out.TermPersistencePort;
import com.plip.user.application.port.out.UserAuthPersistencePort;
import com.plip.user.application.port.out.UserNotificationSettingPersistencePort;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.application.port.out.UserTermsAgreementPersistencePort;
import com.plip.user.application.port.out.UuidGeneratorPort;
import com.plip.user.application.port.out.VerificationTokenPort;
import com.plip.user.domain.model.OtpPurpose;
import com.plip.user.domain.model.Term;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UserAuth;
import com.plip.user.domain.model.UserNotificationSetting;
import com.plip.user.domain.model.UserTermsAgreement;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService implements LocalSignupUseCase, SocialLoginUseCase {

	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final String AUTH_TYPE_LOCAL = "LOCAL";
	private static final String AUTH_TYPE_SOCIAL = "SOCIAL";
	private static final String KAFKA_TOPIC_USER_REGISTERED = "user.registered";
	private static final LocalTime DEFAULT_DIARY_NOTIFY_TIME = LocalTime.of(21, 0);

	private final VerificationTokenPort verificationTokenPort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final UserPersistencePort userPersistencePort;
	private final UserAuthPersistencePort userAuthPersistencePort;
	private final TermPersistencePort termPersistencePort;
	private final UserTermsAgreementPersistencePort userTermsAgreementPersistencePort;
	private final UserNotificationSettingPersistencePort userNotificationSettingPersistencePort;
	private final UuidGeneratorPort uuidGeneratorPort;
	private final OAuthUserInfoPort oAuthUserInfoPort;
	private final EventPublisherPort eventPublisherPort;
	private final AuthTokenService authTokenService;
	private final UserAccountStatusValidator userAccountStatusValidator;
	private final LocalSignupEmailValidator localSignupEmailValidator;

	@Override
	@Transactional
	public SignupResult signup(LocalSignupCommand command) {
		validateVerificationToken(command.getEmail(), command.getVerificationToken());
		validateNickname(command.getNickname());
		localSignupEmailValidator.assertEligibleForSignup(command.getEmail());
		localSignupEmailValidator.releaseExpiredWithdrawnEmail(command.getEmail());

		List<TermAgreementItem> agreements = command.getTermsAgreements() != null
				? command.getTermsAgreements()
				: Collections.emptyList();
		validateTermsAgreements(agreements);

		UuidV7 userUuid = uuidGeneratorPort.generate();
		User savedUser = createUser(userUuid, command.getNickname(), null);

		String encodedPassword = passwordEncoderPort.encode(command.getPassword());
		createLocalAuth(savedUser.getId(), command.getEmail(), encodedPassword);
		if (!agreements.isEmpty()) {
			saveTermsAgreements(savedUser.getId(), agreements);
		}
		createDefaultNotificationSetting(savedUser.getId());

		verificationTokenPort.deleteByEmail(OtpPurpose.SIGNUP, command.getEmail());

		publishUserRegisteredEvent(userUuid, command.getEmail(), command.getNickname());

		AuthTokenResult tokens = authTokenService.issueTokens(userUuid);
		return SignupResult.of(userUuid.toString(), tokens);
	}

	@Override
	@Transactional
	public SignupResult login(SocialLoginCommand command) {
		OAuthUserInfo userInfo = oAuthUserInfoPort.getUserInfo(
				command.getProvider(), command.getAccessToken());
		validateSocialUserInfo(userInfo);

		SignupResult result = userAuthPersistencePort
				.findByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
				.map(this::handleExistingSocialUser)
				.orElseGet(() -> handleNewSocialUser(command, userInfo));

		log.info(
				"소셜 로그인 provider={} providerUserId={} userUuid={} newUser={}",
				userInfo.provider(),
				userInfo.providerUserId(),
				result.getUserUuid(),
				result.isNewUser()
		);
		return result;
	}

	private void validateSocialUserInfo(OAuthUserInfo userInfo) {
		if (userInfo.provider() == null || userInfo.provider().isBlank()) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		String providerUserId = userInfo.providerUserId();
		if (providerUserId == null || providerUserId.isBlank()
				|| "null".equalsIgnoreCase(providerUserId.trim())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private void validateVerificationToken(String email, String token) {
		String storedToken = verificationTokenPort.findByEmail(OtpPurpose.SIGNUP, email);
		if (storedToken == null || !storedToken.equals(token)) {
			throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_INVALID);
		}
	}

	private void validateNickname(String nickname) {
		if (nickname == null || nickname.length() < 2 || nickname.length() > 12) {
			throw new BusinessException(ErrorCode.INVALID_NICKNAME);
		}
	}

	private String normalizeSocialNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "User";
		}
		String trimmed = nickname.trim();
		if (trimmed.length() < 2) {
			return "User";
		}
		if (trimmed.length() > 12) {
			return trimmed.substring(0, 12);
		}
		return trimmed;
	}

	private void validateTermsAgreements(List<TermAgreementItem> agreements) {
		List<Term> requiredTerms = termPersistencePort.findAllActiveRequired();
		if (requiredTerms.isEmpty() && agreements.isEmpty()) {
			return;
		}

		Set<Long> agreedTermIds = agreements.stream()
				.filter(TermAgreementItem::isAgreed)
				.map(TermAgreementItem::getTermId)
				.collect(Collectors.toSet());

		List<Long> requestTermIds = agreements.stream()
				.map(TermAgreementItem::getTermId)
				.toList();

		List<Term> requestedTerms = termPersistencePort.findAllByIdIn(requestTermIds);
		Map<Long, Term> termMap = requestedTerms.stream()
				.collect(Collectors.toMap(Term::getId, t -> t));

		for (TermAgreementItem item : agreements) {
			Term term = termMap.get(item.getTermId());
			if (term == null || !STATUS_ACTIVE.equals(term.getStatus())) {
				throw new BusinessException(ErrorCode.TERM_NOT_FOUND);
			}
		}

		boolean allRequiredAgreed = requiredTerms.stream()
				.allMatch(term -> agreedTermIds.contains(term.getId()));
		if (!allRequiredAgreed) {
			throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
		}
	}

	private User createUser(UuidV7 userUuid, String nickname, String profileImagePath) {
		User user = User.of(null, userUuid, nickname, profileImagePath, STATUS_ACTIVE, null, null, null);
		return userPersistencePort.save(user);
	}

	private void createLocalAuth(Long userId, String email, String encodedPassword) {
		UserAuth userAuth = UserAuth.of(
				null, userId, AUTH_TYPE_LOCAL, email, encodedPassword,
				null, null, null, null, null
		);
		userAuthPersistencePort.save(userAuth);
	}

	private void createSocialAuth(Long userId, String email, String provider, String providerUserId) {
		UserAuth userAuth = UserAuth.of(
				null, userId, AUTH_TYPE_SOCIAL, email, null,
				provider, providerUserId, null, null, null
		);
		userAuthPersistencePort.save(userAuth);
	}

	private void saveTermsAgreements(Long userId, List<TermAgreementItem> agreements) {
		LocalDateTime now = LocalDateTime.now();
		List<UserTermsAgreement> domainAgreements = agreements.stream()
				.map(item -> UserTermsAgreement.of(
						null, userId, item.getTermId(), item.isAgreed(),
						item.isAgreed() ? now : null, null, null, null
				))
				.toList();
		userTermsAgreementPersistencePort.saveAll(domainAgreements);
	}

	private void createDefaultNotificationSetting(Long userId) {
		UserNotificationSetting setting = UserNotificationSetting.of(
				null, userId, true, true, DEFAULT_DIARY_NOTIFY_TIME, null, null
		);
		userNotificationSettingPersistencePort.save(setting);
	}

	private SignupResult handleExistingSocialUser(UserAuth existingAuth) {
		User user = userPersistencePort.findById(existingAuth.getUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
		userAccountStatusValidator.validateLoginEligible(user);
		AuthTokenResult tokens = authTokenService.issueTokens(user.getUserUuid());
		return SignupResult.of(user.getUserUuid().toString(), false, tokens);
	}

	private SignupResult handleNewSocialUser(SocialLoginCommand command, OAuthUserInfo userInfo) {
		List<TermAgreementItem> agreements = command.getTermsAgreements() != null
				? command.getTermsAgreements()
				: Collections.emptyList();

		if (!command.isTermsStepCompleted() && agreements.isEmpty()) {
			throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_REQUIRED);
		}

		validateTermsAgreements(agreements);

		String nickname = normalizeSocialNickname(userInfo.nickname());
		String email = userInfo.email() != null ? userInfo.email() : "";

		UuidV7 userUuid = uuidGeneratorPort.generate();
		User savedUser = createUser(userUuid, nickname, null);

		createSocialAuth(savedUser.getId(), email, userInfo.provider(), userInfo.providerUserId());
		if (!agreements.isEmpty()) {
			saveTermsAgreements(savedUser.getId(), agreements);
		}
		createDefaultNotificationSetting(savedUser.getId());

		publishUserRegisteredEvent(userUuid, email, nickname);

		AuthTokenResult tokens = authTokenService.issueTokens(userUuid);
		return SignupResult.of(userUuid.toString(), true, tokens);
	}

	private void publishUserRegisteredEvent(UuidV7 userUuid, String email, String nickname) {
		try {
			String payload = String.format(
					"{\"userUuid\":\"%s\",\"email\":\"%s\",\"nickname\":\"%s\",\"occurredAt\":\"%s\"}",
					userUuid.toString(), email, nickname, LocalDateTime.now()
			);
			eventPublisherPort.publish(KAFKA_TOPIC_USER_REGISTERED, userUuid.toString(), payload);
		} catch (Exception e) {
			log.warn("user.registered 이벤트 발행 실패 (가입은 완료됨): {}", e.getMessage());
		}
	}
}
