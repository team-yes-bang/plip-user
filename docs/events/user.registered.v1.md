# user.registered (v1)

## 개요

사용자 가입 완료 시 발행되는 이벤트입니다.

- **Topic:** `user.registered`
- **발행 주체:** user-service (가입 TX 후)
- **구독자:** diary-service (다이어리 + 테마 "일상" 자동 생성)

## Payload

```json
{
  "userUuid": "01912345-6789-7abc-def0-123456789abc",
  "email": "user@example.com",
  "nickname": "플립이",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userUuid | String (UUID) | O | 사용자 UUID (UUIDv7) |
| email | String | O | 가입 이메일 (소셜의 경우 빈 문자열 가능) |
| nickname | String | O | 닉네임 |
| occurredAt | String (ISO 8601) | O | 이벤트 발생 시각 |

## Key

- Kafka Message Key: `userUuid`

## 발행 시점

- `POST /api/v1/auth/signup/local` 가입 완료 후
- `POST /api/v1/auth/login/social/{provider}` 신규 소셜 가입 완료 후

## 비고

- 이벤트 발행 실패 시 가입 자체는 롤백되지 않습니다 (at-most-once).
