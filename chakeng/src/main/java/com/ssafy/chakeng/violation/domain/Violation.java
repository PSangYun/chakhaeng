package com.ssafy.chakeng.violation.domain;

import com.ssafy.chakeng.domain.video.Video;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "violations",
        indexes = @Index(name = "idx_vi_video_time", columnList = "video_id, createdAt"))
@Getter
@Setter
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private String type;

    private String plate;
    private String locationText;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

