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
@Table(name = "user_terms_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreementEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "term_id", nullable = false)
	private Long termId;

	@Column(name = "is_agreed", nullable = false)
	private boolean agreed;

	@Column(name = "agreed_at")
	private LocalDateTime agreedAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	@Builder
	private UserTermsAgreementEntity(Long id, Long userId, Long termId, boolean agreed,
			LocalDateTime agreedAt, LocalDateTime revokedAt) {
		this.id = id;
		this.userId = userId;
		this.termId = termId;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
		this.revokedAt = revokedAt;
	}
}
