package com.ssafy.chakeng.domain.video;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="videos")
@Getter
@Setter
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable=false)
    private UUID ownerId;

    @Column(nullable=false)
    private String filename;

    @Column(nullable=false)
    private String status;

    @Column(nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

