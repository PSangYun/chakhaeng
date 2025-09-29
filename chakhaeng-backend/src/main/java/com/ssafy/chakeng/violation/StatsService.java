package com.ssafy.chakeng.violation;

import com.ssafy.chakeng.report.ReportRepository;
import com.ssafy.chakeng.violation.dto.TodayStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ViolationRepository violationRepo;
    private final ReportRepository reportRepo;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public TodayStatsResponse getTodayStats(UUID ownerId) {
        OffsetDateTime start = LocalDate.now(ZONE).atStartOfDay(ZONE).toOffsetDateTime();
        long detected = violationRepo.countToday(ownerId, start);
        long reported = reportRepo.countTodayCompleted(ownerId, start);
        return new TodayStatsResponse(detected, reported);
    }
}

