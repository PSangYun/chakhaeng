package com.ssafy.chakeng.violation.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ViolationResponse {
    UUID id;
    UUID videoId;
    String type;
    String plate;
    String locationText;
    OffsetDateTime occurredAt;
    OffsetDateTime createdAt;
}