package com.plip.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgitMember {

	private Long id;
	private UUID agitUuid;
	private UUID userUuid;
	private String role;
	private boolean active;
	private String agitName;

	public static NotificationAgitMember of(
			Long id,
			UUID agitUuid,
			UUID userUuid,
			String role,
			boolean active,
			String agitName
	) {
		NotificationAgitMember member = new NotificationAgitMember();
		member.id = id;
		member.agitUuid = agitUuid;
		member.userUuid = userUuid;
		member.role = role;
		member.active = active;
		member.agitName = agitName;
		return member;
	}

	public void activate(String role, String agitName) {
		this.active = true;
		if (role != null && !role.isBlank()) {
			this.role = role;
		}
		if (agitName != null && !agitName.isBlank()) {
			this.agitName = agitName;
		}
	}

	public void deactivate() {
		this.active = false;
	}

	public void updateRole(String role) {
		if (role != null && !role.isBlank()) {
			this.role = role;
		}
	}

	public boolean isHost() {
		return "HOST".equalsIgnoreCase(role);
	}
}
