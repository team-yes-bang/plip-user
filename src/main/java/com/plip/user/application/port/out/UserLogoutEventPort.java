package com.plip.user.application.port.out;

import com.plip.user.domain.model.UuidV7;

public interface UserLogoutEventPort {

	void publishLogout(UuidV7 userUuid);
}
