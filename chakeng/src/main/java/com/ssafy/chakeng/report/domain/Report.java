package com.ssafy.chakeng.report.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name="reports",
        indexes = @Index(name="idx_reports_owner_status_time", columnList="ownerId, status, createdAt")
)
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable=false)
    private UUID ownerId;

    @Column(nullable=false)
    private String violationType;

    @Column(nullable=false)
    private String location;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false, length = 4000)
    private String description;

    @Column(nullable=false)
    private String plateNumber;

    @Column(nullable=false)
    private OffsetDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ReportStatus status;

    @Column(nullable=false)
    private OffsetDateTime createdAt;
}
