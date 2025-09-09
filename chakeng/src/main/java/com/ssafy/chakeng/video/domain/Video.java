package com.ssafy.chakeng.video.domain;

import com.ssafy.chakeng.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_owner_time", columnList = "owner_id, created_at"),
        @Index(name = "idx_videos_status_time", columnList = "status, created_at")
})
@Getter @Setter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private String originalName;

    private String contentType;
    private Long sizeBytes;
    private Integer durationSec;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public enum VideoStatus { UPLOADED, TRANSCODING, READY, FAILED }
}

