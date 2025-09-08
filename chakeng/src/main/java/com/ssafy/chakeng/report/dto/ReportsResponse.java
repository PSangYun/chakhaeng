package com.ssafy.chakeng.report.dto;

import com.ssafy.chakeng.report.domain.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ReportsResponse {
    UUID id;
    String violationType;
    String location;
    String title;
    String plateNumber;
    OffsetDateTime occurredAt;  // 발생 시각
    ReportStatus status;        // PENDING / ...
    OffsetDateTime createdAt;   // 신고 생성 시각
}
