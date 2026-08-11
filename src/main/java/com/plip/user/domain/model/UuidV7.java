package com.plip.user.domain.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * UUIDv7 값 객체 (BINARY(16) 저장, RFC 9562 time-ordered UUID).
 */
public final class UuidV7 {

	private static final int BYTE_LENGTH = 16;

	private final byte[] value;

	private UuidV7(byte[] value) {
		this.value = value;
	}

	public static UuidV7 of(byte[] bytes) {
		if (bytes == null || bytes.length != BYTE_LENGTH) {
			throw new IllegalArgumentException("UUIDv7는 16바이트 BINARY 값이어야 합니다.");
		}
		return new UuidV7(Arrays.copyOf(bytes, BYTE_LENGTH));
	}

	public static UuidV7 of(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ByteBuffer buffer = ByteBuffer.wrap(new byte[BYTE_LENGTH]);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return new UuidV7(buffer.array());
	}

	public static UuidV7 parse(String canonical) {
		return of(UUID.fromString(canonical));
	}

	public byte[] toBytes() {
		return Arrays.copyOf(value, BYTE_LENGTH);
	}

	public UUID toUuid() {
		ByteBuffer buffer = ByteBuffer.wrap(value);
		return new UUID(buffer.getLong(), buffer.getLong());
	}

	@Override
	public String toString() {
		return toUuid().toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof UuidV7 uuidV7)) {
			return false;
		}
		return Arrays.equals(value, uuidV7.value);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
