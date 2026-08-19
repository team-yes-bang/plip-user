package com.plip.user.application.service;

import com.plip.user.application.port.in.RestoreUserUseCase;
import com.plip.user.application.port.out.UserPersistencePort;
import com.plip.user.domain.exception.UserDomainException;
import com.plip.user.domain.model.User;
import com.plip.user.domain.model.UuidV7;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestoreUserService implements RestoreUserUseCase {

	private final UserPersistencePort userPersistencePort;
	private final UserDomainExceptionMapper userDomainExceptionMapper;

	@Override
	@Transactional
	public void restore(String userUuidValue) {
		UuidV7 userUuid = UuidV7.parse(userUuidValue);
		User user = userPersistencePort.findByUserUuid(userUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		try {
			user.restore(LocalDateTime.now());
		} catch (UserDomainException exception) {
			throw userDomainExceptionMapper.toBusinessException(exception);
		}

		userPersistencePort.save(user);
	}
}
