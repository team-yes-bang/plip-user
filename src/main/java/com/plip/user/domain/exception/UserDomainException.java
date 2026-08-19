package com.plip.user.domain.exception;

import lombok.Getter;

@Getter
public class UserDomainException extends RuntimeException {

	private final UserDomainError error;

	public UserDomainException(UserDomainError error) {
		super(error.name());
		this.error = error;
	}
}
