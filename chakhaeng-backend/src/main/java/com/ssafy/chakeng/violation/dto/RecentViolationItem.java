package com.ssafy.chakeng.violation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RecentViolationItem {
    private UUID violationId;
    private String type;
    private String typeLabel;
    private String plate;
    private String locationText;
    private OffsetDateTime occurredAt;
}
