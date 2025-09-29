package com.ssafy.chakeng.fcm.domain;

import com.ssafy.chakeng.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "device_tokens",
        indexes = @Index(name="idx_device_tokens_user_active", columnList = "user_id, active"))
@Getter
@Setter
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false, unique=true, length=2048)
    private String token;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Platform platform;

    @Column(nullable=false)
    private boolean active = true;

    public enum Platform { ANDROID, IOS, WEB }
}
