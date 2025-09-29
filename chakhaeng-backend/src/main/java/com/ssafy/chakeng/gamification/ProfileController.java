package com.ssafy.chakeng.gamification;

import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.gamification.dto.BadgeDto;
import com.ssafy.chakeng.gamification.dto.MissionDto;
import com.ssafy.chakeng.gamification.dto.UserProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok("성공",profileService.getUserProfile(userId)));
    }

    @GetMapping("/badges")
    public ResponseEntity<ApiResponse<List<BadgeDto>>> getUserBadges(
            @RequestAttribute("userId") UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("성공",profileService.getUserBadges(userId)));
    }

    @GetMapping("/missions")
    public ResponseEntity<ApiResponse<List<MissionDto>>>getUserMissions( @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok("성공",profileService.getUserMissions(userId)));
    }
}
