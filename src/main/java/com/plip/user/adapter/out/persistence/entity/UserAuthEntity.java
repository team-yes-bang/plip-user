package com.plip.user.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auths")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthEntity extends SoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "auth_type", nullable = false, length = 20)
	private String authType;

	@Column(nullable = false)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(length = 50)
	private String provider;

	@Column(name = "provider_user_id")
	private String providerUserId;

	@Builder
	private UserAuthEntity(Long id, Long userId, String authType, String email,
			String passwordHash, String provider, String providerUserId) {
		this.id = id;
		this.userId = userId;
		this.authType = authType;
		this.email = email;
		this.passwordHash = passwordHash;
		this.provider = provider;
		this.providerUserId = providerUserId;
	}

	public void updatePasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void softDelete(LocalDateTime deletedAt) {
		applyDeletedAt(deletedAt);
	}
}
