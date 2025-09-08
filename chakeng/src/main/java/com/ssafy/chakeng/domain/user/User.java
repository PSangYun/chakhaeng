package com.ssafy.chakeng.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    @Column(unique = true) private String email;
    private String name;
    private String picture;
    @Enumerated(EnumType.STRING) private AuthProvider provider;
    private String providerId;
    private boolean active = true;

    public enum AuthProvider { GOOGLE, KAKAO, NAVER, APPLE }
}



