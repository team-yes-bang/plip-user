# user.logout (v1)

## 개요

사용자 로그아웃 시 발행되는 이벤트입니다.

- **Topic:** `user.logout`
- **발행 주체:** user-service (로그아웃 TX 후)
- **구독자:** notification-service (device token 비활성화)

## Payload

```json
{
  "userUuid": "01912345-6789-7abc-def0-123456789abc",
  "occurredAt": "2026-08-18T14:00:00"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userUuid | String (UUID) | O | 사용자 UUID (UUIDv7) |
| occurredAt | String (ISO 8601) | O | 이벤트 발생 시각 |

## Key

- Kafka Message Key: `userUuid`

## 발행 시점

- `POST /api/v1/auth/logout` 로그아웃 성공 후 (Redis Refresh Token 파기 완료 후)

## 비고

- 이벤트 발행 실패 시 로그아웃 자체는 롤백되지 않습니다 (at-most-once).
- user-service는 `user_device_tokens` 테이블을 보유하지 않으며, notification-service가 구독 후 해당 유저의 device token을 비활성화합니다.
