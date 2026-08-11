package com.plip.user.adapter.out.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import com.plip.user.application.port.out.UuidGeneratorPort;
import com.plip.user.domain.model.UuidV7;
import org.springframework.stereotype.Component;

@Component
public class UuidV7GeneratorAdapter implements UuidGeneratorPort {

	@Override
	public UuidV7 generate() {
		return UuidV7.of(UuidCreator.getTimeOrderedEpoch());
	}
}
