package com.ssafy.chakeng.gamification.domain;

import com.ssafy.chakeng.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Mission {
    @Id
    private String id;

    private String title;
    private String description;
    private String iconUrl;
    private boolean isCompleted;

    private int currentProgress;
    private int targetProgress;
    private String rewardName;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}

