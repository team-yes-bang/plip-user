package com.plip.user.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
		name = "notification_agit_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_notification_agit_member",
				columnNames = {"agit_uuid", "user_uuid"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgitMemberEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "agit_uuid", nullable = false, length = 16)
	private UUID agitUuid;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "user_uuid", nullable = false, length = 16)
	private UUID userUuid;

	@Column(name = "role", nullable = false, length = 20)
	private String role;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "agit_name", length = 200)
	private String agitName;

	@Builder
	private NotificationAgitMemberEntity(
			Long id,
			UUID agitUuid,
			UUID userUuid,
			String role,
			boolean active,
			String agitName
	) {
		this.id = id;
		this.agitUuid = agitUuid;
		this.userUuid = userUuid;
		this.role = role;
		this.active = active;
		this.agitName = agitName;
	}
}
