# user.notification-setting-updated (v1)

## 개요

사용자 알림 설정 변경 시 발행되는 이벤트입니다.

- **Topic:** `user.notification-setting-updated`
- **발행 주체:** user-service (알림 설정 PUT 성공 후)
- **구독자:** notification-service (푸시 스케줄·발송 정책 반영)

## Payload

```json
{
  "userUuid": "01912345-6789-7abc-def0-123456789abc",
  "agitNotifyEnabled": true,
  "diaryNotifyEnabled": true,
  "diaryNotifyTime": "21:00",
  "occurredAt": "2026-08-24T12:00:00"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userUuid | String (UUID) | O | 사용자 UUID (UUIDv7) |
| agitNotifyEnabled | Boolean | O | 아지트 알림 허용 여부 |
| diaryNotifyEnabled | Boolean | O | 다이어리 알림 허용 여부 |
| diaryNotifyTime | String (HH:mm[:ss]) | O | 다이어리 알림 시각 |
| occurredAt | String (ISO 8601) | O | 이벤트 발생 시각 |

## Key

- Kafka Message Key: `userUuid`

## 발행 시점

- `PUT /api/v1/users/me/notification-settings` 저장 성공 후

## 비고

- 이벤트 발행 실패 시 설정 저장 자체는 롤백되지 않습니다 (at-most-once).
- user-service는 `user_device_tokens` 테이블을 보유하지 않으며, notification-service가 구독 후 발송 정책을 갱신합니다.
