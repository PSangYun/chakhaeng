package com.ssafy.chakeng.report.domain;

import com.ssafy.chakeng.user.domain.User;
import com.ssafy.chakeng.video.domain.Video;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_owner_status_time", columnList = "owner_id, status, created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_owner_video", columnNames = {"owner_id", "video_id"})
})
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable=false)
    private UUID ownerId;

    @Column(nullable=true)
    private UUID videoId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String violationType;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private OffsetDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
