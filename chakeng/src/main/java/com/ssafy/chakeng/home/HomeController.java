package com.ssafy.chakeng.home;

import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.violation.RecentViolationService;
import com.ssafy.chakeng.violation.StatsService;
import com.ssafy.chakeng.violation.dto.RecentViolationItem;
import com.ssafy.chakeng.violation.dto.TodayStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final StatsService statsService;
    private final RecentViolationService recentService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayStatsResponse>> today(
            @RequestAttribute("userId") UUID userId // JWT 필터에서 주입
    ) {
        var data = statsService.getTodayStats(userId);
        return ResponseEntity.ok(ApiResponse.ok("Today stats fetched.", data));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecentViolationItem>>> recent(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "3") int limit
    ) {
        var data = recentService.getRecent(userId, limit);
        return ResponseEntity.ok(ApiResponse.ok("Recent violations fetched.", data));
    }
}
