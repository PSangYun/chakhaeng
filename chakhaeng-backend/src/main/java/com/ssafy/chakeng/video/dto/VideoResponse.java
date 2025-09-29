package com.ssafy.chakeng.video.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class VideoResponse {
    UUID id;
    String objectKey;
    String originalName;
    String status;
    OffsetDateTime createdAt;
}
