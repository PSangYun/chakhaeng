package com.ssafy.chakeng.stats;

import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.stats.dto.ReportStatistics;
import com.ssafy.chakeng.stats.dto.ViolationStatistics;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/violations")
    public ResponseEntity<ApiResponse<ViolationStatistics>> getViolationStats(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.ok("생성 완료",statisticsService.getViolationStats(userId, from, to)));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<ReportStatistics>> getReportStats(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.ok("생성 완료",statisticsService.getReportStats(userId, from, to)));
    }
}