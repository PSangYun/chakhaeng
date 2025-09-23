package com.ssafy.chakeng.fcm;

import com.google.firebase.messaging.*;
import com.ssafy.chakeng.fcm.domain.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PushService {

    private final DeviceTokenService deviceTokenService;

    public void sendViolationDetected(UUID userId, UUID violationId, UUID videoId,
                                      String type, OffsetDateTime occurredAt) throws FirebaseMessagingException {

        List<DeviceToken> tokens = deviceTokenService.activeTokens(userId);
        if (tokens.isEmpty()) return;

        String title = "위반 감지";
        String body  = String.format("%s 발생", type);

        Map<String, String> data = Map.of(
                "kind", "VIOLATION_DETECTED",
                "violationId", violationId.toString(),
                "videoId", videoId.toString(),
                "type", type,
                "occurredAt", occurredAt.toString()
        );

        List<Message> messages = tokens.stream().map(dt -> {
            AndroidConfig android = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setChannelId("violation")
                            .build())
                    .build();

            ApnsConfig apns = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setAlert(ApsAlert.builder().setTitle(title).setBody(body).build())
                            .setSound("default")
                            .setBadge(1)
                            .build())
                    .putHeader("apns-priority", "10")
                    .build();

            return Message.builder()
                    .setToken(dt.getToken())
                    .putAllData(data)                // 데이터 페이로드(권장)
                    .setNotification(Notification.builder() // Notification(백그라운드 표시)
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(android)
                    .setApnsConfig(apns)
                    .build();
        }).toList();

        BatchResponse resp = FirebaseMessaging.getInstance().sendEach(messages);

    }
}

