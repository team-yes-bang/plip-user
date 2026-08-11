package com.plip.user.application.port.out;

import com.plip.user.domain.model.UuidV7;

public interface UuidGeneratorPort {

	UuidV7 generate();
}
