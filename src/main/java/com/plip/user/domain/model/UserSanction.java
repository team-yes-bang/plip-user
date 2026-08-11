package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSanction {

	private Long id;
	private Long userId;
	private UuidV7 adminUuid;
	private String sanctionType;
	private int durationDays;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String status;
	private String reason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static UserSanction of(
			Long id,
			Long userId,
			UuidV7 adminUuid,
			String sanctionType,
			int durationDays,
			LocalDateTime startDate,
			LocalDateTime endDate,
			String status,
			String reason,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		UserSanction sanction = new UserSanction();
		sanction.id = id;
		sanction.userId = userId;
		sanction.adminUuid = adminUuid;
		sanction.sanctionType = sanctionType;
		sanction.durationDays = durationDays;
		sanction.startDate = startDate;
		sanction.endDate = endDate;
		sanction.status = status;
		sanction.reason = reason;
		sanction.createdAt = createdAt;
		sanction.updatedAt = updatedAt;
		return sanction;
	}
}
