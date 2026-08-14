package com.plip.user.application.port.in;

public interface TokenReissueUseCase {

	AuthTokenResult reissue(String refreshToken);
}
