package com.ssafy.chakeng.gamification;

import com.ssafy.chakeng.gamification.dto.BadgeDto;
import com.ssafy.chakeng.gamification.dto.MissionDto;
import com.ssafy.chakeng.gamification.dto.UserProfileDto;
import com.ssafy.chakeng.gamification.repository.BadgeRepository;
import com.ssafy.chakeng.gamification.repository.MissionRepository;
import com.ssafy.chakeng.user.UserRepository;
import com.ssafy.chakeng.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final MissionRepository missionRepository;

    public ProfileService(UserRepository userRepository,
                          BadgeRepository badgeRepository,
                          MissionRepository missionRepository) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.missionRepository = missionRepository;
    }

    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserProfileDto(user.getName(), user.getEmail(), user.getPicture());
    }

    public List<BadgeDto> getUserBadges(UUID userId) {
        return badgeRepository.findByUserId(userId).stream()
                .map(b -> new BadgeDto(
                        b.getId(),
                        b.getName(),
                        b.getDescription(),
                        b.getIconUrl(),
                        b.isUnlocked()
                ))
                .toList();
    }

    public List<MissionDto> getUserMissions(UUID userId) {
        return missionRepository.findByUserId(userId).stream()
                .map(m -> new MissionDto(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getIconUrl(),
                        m.isCompleted(),
                        m.getCurrentProgress(),
                        m.getTargetProgress(),
                        m.getRewardName()
                ))
                .toList();
    }
}

