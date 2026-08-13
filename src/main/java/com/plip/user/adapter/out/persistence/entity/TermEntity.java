package com.plip.user.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "term_uuid", nullable = false, length = 16, unique = true)
	private UUID termUuid;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "content_path", nullable = false, length = 256)
	private String contentPath;

	@Column(name = "term_code", nullable = false, length = 50)
	private String termCode;

	@Column(nullable = false, length = 20)
	private String version;

	@Column(name = "is_required", nullable = false)
	private boolean required;

	@Column(nullable = false, length = 20)
	private String status;
}
