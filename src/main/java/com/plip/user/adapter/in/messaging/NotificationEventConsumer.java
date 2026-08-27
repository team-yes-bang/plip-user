package com.plip.user.adapter.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.user.application.port.in.IngestNotificationCommand;
import com.plip.user.application.service.NotificationIngestService;
import com.plip.user.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class NotificationEventConsumer {

	private final ObjectMapper objectMapper;
	private final NotificationIngestService notificationIngestService;

	@KafkaListener(
			topics = {
					NotificationEventTopics.AGIT_CREATED,
					NotificationEventTopics.AGIT_JOIN_REQUESTED,
					NotificationEventTopics.AGIT_MEMBER_JOINED,
					NotificationEventTopics.AGIT_MEMBER_LEFT,
					NotificationEventTopics.AGIT_MEMBER_BANNED,
					NotificationEventTopics.AGIT_HOST_TRANSFERRED,
					NotificationEventTopics.TOPIC_CREATED,
					NotificationEventTopics.TOPIC_BOUND,
					NotificationEventTopics.TOPIC_STARTED,
					NotificationEventTopics.TOPIC_VIDEO_ATTACHED,
					NotificationEventTopics.DIARY_VIDEO_UPLOADED,
					NotificationEventTopics.CHAT_MESSAGE_SENT
			},
			groupId = "user-notification-inbox"
	)
	public void consume(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		if (payload == null || payload.isBlank()) {
			return;
		}
		try {
			JsonNode node = objectMapper.readTree(payload);
			switch (topic) {
				case NotificationEventTopics.AGIT_CREATED -> onAgitCreated(node);
				case NotificationEventTopics.AGIT_JOIN_REQUESTED -> onJoinRequested(node);
				case NotificationEventTopics.AGIT_MEMBER_JOINED -> onMemberJoined(node);
				case NotificationEventTopics.AGIT_MEMBER_LEFT, NotificationEventTopics.AGIT_MEMBER_BANNED
						-> onMemberInactive(node);
				case NotificationEventTopics.AGIT_HOST_TRANSFERRED -> onHostTransferred(node);
				case NotificationEventTopics.TOPIC_CREATED -> onTopicCreated(node);
				case NotificationEventTopics.TOPIC_BOUND, NotificationEventTopics.TOPIC_STARTED -> onTopicBound(node);
				case NotificationEventTopics.TOPIC_VIDEO_ATTACHED -> onTopicVideoAttached(node);
				case NotificationEventTopics.DIARY_VIDEO_UPLOADED -> onDiaryVideoUploaded(node);
				case NotificationEventTopics.CHAT_MESSAGE_SENT -> onChatMessageSent(node);
				default -> {
				}
			}
		} catch (Exception exception) {
			log.warn("notification ingest skip topic={} reason={}", topic, exception.getMessage());
		}
	}

	private void onAgitCreated(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID hostUuid = uuid(node, "hostUserUuid", "host_user_uuid");
		String agitName = text(node, "agitName", "agit_name");
		notificationIngestService.upsertMember(agitUuid, hostUuid, "HOST", true, agitName);
		ingest(
				hostUuid,
				NotificationType.CREATION,
				"아지트가 생성되었습니다",
				agitName == null ? "새 아지트가 준비되었습니다." : agitName + " 아지트가 생성되었습니다.",
				agitLink(agitUuid, ""),
				agitUuid == null ? null : agitUuid.toString(),
				agitUuid,
				dedupe("creation", agitUuid)
		);
	}

	private void onJoinRequested(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID requester = uuid(node, "userUuid", "user_uuid");
		String nickname = text(node, "nickname");
		String ampId = text(node, "ampId", "amp_id");
		UUID hostUuid = firstNonNull(
				uuid(node, "hostUserUuid", "host_user_uuid"),
				notificationIngestService.hostUserUuid(agitUuid)
		);
		if (hostUuid == null || hostUuid.equals(requester)) {
			return;
		}
		String agitName = firstNonBlank(text(node, "agitName", "agit_name"), notificationIngestService.agitName(agitUuid));
		ingest(
				hostUuid,
				NotificationType.JOIN_REQUEST,
				"입장 요청",
				(nickname == null ? "멤버" : nickname) + "님이 "
						+ (agitName == null ? "아지트" : agitName) + " 입장을 요청했습니다.",
				agitLink(agitUuid, "/manage"),
				ampId,
				agitUuid,
				dedupe("join-request", agitUuid, requester, ampId)
		);
	}

	private void onMemberJoined(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID userUuid = uuid(node, "userUuid", "user_uuid");
		String nickname = text(node, "nickname");
		String role = firstNonBlank(text(node, "role"), "GUEST");
		notificationIngestService.upsertMember(
				agitUuid,
				userUuid,
				role,
				true,
				notificationIngestService.agitName(agitUuid)
		);
		UUID hostUuid = notificationIngestService.hostUserUuid(agitUuid);
		if (hostUuid == null || hostUuid.equals(userUuid)) {
			return;
		}
		ingest(
				hostUuid,
				NotificationType.CREATION,
				"새 멤버가 입장했습니다",
				(nickname == null ? "멤버" : nickname) + "님이 아지트에 입장했습니다.",
				agitLink(agitUuid, "/members"),
				userUuid == null ? null : userUuid.toString(),
				agitUuid,
				dedupe("member-joined", agitUuid, userUuid)
		);
	}

	private void onMemberInactive(JsonNode node) {
		notificationIngestService.upsertMember(
				uuid(node, "agitUuid", "agit_uuid"),
				uuid(node, "userUuid", "user_uuid"),
				null,
				false,
				null
		);
	}

	private void onHostTransferred(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		String agitName = notificationIngestService.agitName(agitUuid);
		notificationIngestService.upsertMember(
				agitUuid,
				uuid(node, "previousHostUserUuid", "previous_host_user_uuid"),
				"GUEST",
				true,
				agitName
		);
		notificationIngestService.upsertMember(
				agitUuid,
				uuid(node, "newHostUserUuid", "new_host_user_uuid"),
				"HOST",
				true,
				agitName
		);
	}

	private void onTopicCreated(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID topicUuid = uuid(node, "topicUuid", "topic_uuid", "topicId");
		UUID creator = uuid(node, "creatorUuid", "creator_uuid", "userUuid", "user_uuid");
		String title = firstNonBlank(text(node, "title"), "새 토픽");
		fanOutExcept(
				agitUuid,
				creator,
				NotificationType.TOPIC,
				"새 토픽",
				title + " 토픽이 열렸습니다.",
				topicLink(agitUuid, topicUuid),
				topicUuid == null ? null : topicUuid.toString(),
				dedupe("topic", topicUuid)
		);
	}

	private void onTopicBound(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		String topicId = text(node, "topicId", "topic_id", "topicUuid", "topic_uuid");
		UUID topicUuid = parseUuid(topicId);
		fanOutExcept(
				agitUuid,
				null,
				NotificationType.TOPIC,
				"토픽 시작",
				"아지트에 토픽이 연결되었습니다.",
				topicLink(agitUuid, topicUuid),
				topicId,
				dedupe("topic-bound", agitUuid, topicId)
		);
	}

	private void onTopicVideoAttached(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID topicUuid = uuid(node, "topicUuid", "topic_uuid");
		UUID videoUuid = uuid(node, "videoUuid", "video_uuid");
		UUID actor = uuid(node, "userUuid", "user_uuid");
		fanOutExcept(
				agitUuid,
				actor,
				NotificationType.POST,
				"새로운 글",
				"아지트 피드에 새 영상이 올라왔습니다.",
				videoUuid == null ? topicLink(agitUuid, topicUuid) : "/viewer/" + videoUuid,
				videoUuid == null ? null : videoUuid.toString(),
				dedupe("post-topic", videoUuid)
		);
	}

	private void onDiaryVideoUploaded(JsonNode node) {
		UUID userUuid = uuid(node, "userUuid", "user_uuid");
		UUID videoUuid = uuid(node, "videoUuid", "video_uuid");
		String caption = text(node, "caption");
		ingest(
				userUuid,
				NotificationType.POST,
				"다이어리에 새 글",
				caption == null || caption.isBlank() ? "다이어리에 새 영상이 저장되었습니다." : caption,
				"/diary",
				videoUuid == null ? null : videoUuid.toString(),
				null,
				dedupe("post-diary", videoUuid)
		);
	}

	private void onChatMessageSent(JsonNode node) {
		UUID agitUuid = uuid(node, "agitUuid", "agit_uuid");
		UUID sender = uuid(node, "senderUserUuid", "sender_user_uuid", "userUuid", "user_uuid");
		UUID messageId = uuid(node, "messageId", "message_id");
		String preview = text(node, "contentPreview", "content_preview", "content");
		String body = preview == null || preview.isBlank() ? "새 메시지가 도착했습니다." : preview;
		fanOutExcept(
				agitUuid,
				sender,
				NotificationType.CHAT,
				"새 채팅 메시지",
				body,
				messageId == null ? agitLink(agitUuid, "/chat") : agitLink(agitUuid, "/chat/m/" + messageId),
				messageId == null ? null : messageId.toString(),
				dedupe("chat", messageId)
		);
	}

	private void fanOutExcept(
			UUID agitUuid,
			UUID except,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			String dedupeKey
	) {
		for (UUID recipient : notificationIngestService.activeMemberUuids(agitUuid)) {
			if (except != null && except.equals(recipient)) {
				continue;
			}
			ingest(recipient, type, title, body, deepLink, resourceId, agitUuid, dedupeKey + ":" + recipient);
		}
	}

	private void ingest(
			UUID recipient,
			NotificationType type,
			String title,
			String body,
			String deepLink,
			String resourceId,
			UUID agitUuid,
			String dedupeKey
	) {
		if (recipient == null) {
			return;
		}
		notificationIngestService.ingest(new IngestNotificationCommand(
				recipient,
				type,
				title,
				body,
				deepLink,
				resourceId,
				agitUuid,
				dedupeKey
		));
	}

	private static String agitLink(UUID agitUuid, String suffix) {
		if (agitUuid == null) {
			return "/agit";
		}
		return "/agit/" + agitUuid + (suffix == null ? "" : suffix);
	}

	private static String topicLink(UUID agitUuid, UUID topicUuid) {
		if (agitUuid == null) {
			return "/agit";
		}
		if (topicUuid == null) {
			return "/agit/" + agitUuid + "/topics";
		}
		return "/agit/" + agitUuid + "/topics/" + topicUuid;
	}

	private static String dedupe(Object... parts) {
		StringBuilder builder = new StringBuilder();
		for (Object part : parts) {
			if (builder.length() > 0) {
				builder.append(':');
			}
			builder.append(part == null ? "-" : part);
		}
		return builder.toString();
	}

	private static String text(JsonNode node, String... names) {
		for (String name : names) {
			JsonNode value = node.get(name);
			if (value != null && !value.isNull() && !value.asText().isBlank()) {
				return value.asText();
			}
		}
		return null;
	}

	private static UUID uuid(JsonNode node, String... names) {
		return parseUuid(text(node, names));
	}

	private static UUID parseUuid(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static UUID firstNonNull(UUID... values) {
		for (UUID value : values) {
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}
}
