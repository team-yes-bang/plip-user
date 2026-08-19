package com.plip.user.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected void applyDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}
}
