-- PLIP User Service DDL (plip_user)
-- PK 컬럼명: id (BIGINT AUTO_INCREMENT)
-- 외부 API·JWT 식별자: user_uuid / term_uuid (BINARY(16), UUIDv7)
-- DB FK 없음 — 애플리케이션 레이어에서 참조 무결성·삭제 처리
-- 로컬/소셜 이메일 별도 계정 정책: 동일 이메일이라도 auth_type이 다르면 별개 계정

CREATE TABLE users (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_uuid           BINARY(16)   NOT NULL,
    nickname            VARCHAR(100) NOT NULL,
    profile_image_path  VARCHAR(255) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME     NULL,
    UNIQUE KEY uk_users_uuid (user_uuid),
    INDEX idx_users_status_deleted (status, deleted_at)
);

CREATE TABLE user_auths (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    auth_type           VARCHAR(20)  NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NULL,
    provider            VARCHAR(50)  NULL,
    provider_user_id    VARCHAR(255) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME     NULL,
    -- 로컬 이메일 유니크: 동일 auth_type=LOCAL 내에서만 중복 방지
    active_local_email  VARCHAR(255) AS (
        IF(deleted_at IS NULL AND auth_type = 'LOCAL', email, NULL)
    ) STORED,
    -- 소셜 키 유니크: provider+provider_user_id 조합
    active_social_key   VARCHAR(320) AS (
        IF(
            deleted_at IS NULL AND provider IS NOT NULL AND provider_user_id IS NOT NULL,
            CONCAT(provider, ':', provider_user_id),
            NULL
        )
    ) STORED,
    UNIQUE KEY uk_user_auths_active_local_email (active_local_email),
    UNIQUE KEY uk_user_auths_active_social (active_social_key),
    INDEX idx_user_auths_user_id (user_id)
);

CREATE TABLE terms (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    term_uuid         BINARY(16)   NOT NULL,
    title             VARCHAR(255) NOT NULL,
    content_path      VARCHAR(256) NOT NULL,        -- S3/CDN 약관 파일 경로
    term_code         VARCHAR(50)  NOT NULL,         -- SERVICE, PRIVACY, MARKETING 등
    version           VARCHAR(20)  NOT NULL DEFAULT 'v1.0',
    is_required       BOOLEAN      NOT NULL DEFAULT TRUE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, DEPRECATED
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_terms_uuid (term_uuid),
    UNIQUE KEY uk_terms_code_version (term_code, version)
);

CREATE TABLE user_terms_agreements (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    term_id           BIGINT       NOT NULL,
    is_agreed         BOOLEAN      NOT NULL DEFAULT FALSE,
    agreed_at         DATETIME     NULL,
    revoked_at        DATETIME     NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_term (user_id, term_id),
    INDEX idx_user_terms_agreements_user_id (user_id),
    INDEX idx_user_terms_agreements_term_id (term_id)
);

CREATE TABLE user_notification_settings (
    id                   BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT    NOT NULL,
    agit_notify_enabled  BOOLEAN   NOT NULL DEFAULT TRUE,
    diary_notify_enabled BOOLEAN   NOT NULL DEFAULT TRUE,
    diary_notify_time    TIME      NOT NULL DEFAULT '21:00:00',
    created_at           DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notification_user (user_id)
);

CREATE TABLE user_sanctions (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    admin_uuid        BINARY(16)   NOT NULL,
    sanction_type     VARCHAR(50)  NOT NULL,
    duration_days     INT          NOT NULL,
    start_date        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date          DATETIME     NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    reason            VARCHAR(255) NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sanctions_user_status_end (user_id, status, end_date),
    INDEX idx_sanctions_user_created (user_id, created_at DESC)
);
