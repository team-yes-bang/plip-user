package com.plip.user.adapter.out.persistence.entity;

import com.plip.user.domain.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
		name = "user_notifications",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_user_notification_dedupe",
				columnNames = {"recipient_user_uuid", "dedupe_key"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "recipient_user_uuid", nullable = false, length = 16)
	private UUID recipientUserUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private NotificationType type;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "body", length = 500)
	private String body;

	@Column(name = "deep_link", length = 500)
	private String deepLink;

	@Column(name = "resource_id", length = 64)
	private String resourceId;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "agit_uuid", length = 16)
	private UUID agitUuid;

	@Column(name = "dedupe_key", nullable = false, length = 128)
	private String dedupeKey;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Builder
	private UserNotificationEntity(
			Long id,
			UUID recipientUserUuid,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			UUID agitUuid,
			String dedupeKey,
			LocalDateTime readAt
	) {
		this.id = id;
		this.recipientUserUuid = recipientUserUuid;
		this.type = type;
		this.title = title;
		this.body = body;
		this.deepLink = deepLink;
		this.resourceId = resourceId;
		this.agitUuid = agitUuid;
		this.dedupeKey = dedupeKey;
		this.readAt = readAt;
	}

	public void markRead(LocalDateTime now) {
		if (this.readAt == null) {
			this.readAt = now;
		}
	}
}
