package com.plip.user.application.port.out;

import com.plip.user.domain.model.UuidV7;

public interface TokenProviderPort {

	String createAccessToken(UuidV7 userUuid);

	String createRefreshToken(UuidV7 userUuid);

	UuidV7 parseAccessToken(String token);

	UuidV7 parseRefreshToken(String token);
}
