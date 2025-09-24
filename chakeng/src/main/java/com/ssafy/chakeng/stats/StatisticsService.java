package com.ssafy.chakeng.stats;

import com.ssafy.chakeng.report.ReportRepository;
import com.ssafy.chakeng.report.domain.Report;
import com.ssafy.chakeng.stats.dto.*;
import com.ssafy.chakeng.violation.ViolationRepository;
import com.ssafy.chakeng.violation.domain.Violation;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ViolationRepository violationRepository;
    private final ReportRepository reportRepository;

    public StatisticsService(ViolationRepository violationRepository,
                             ReportRepository reportRepository) {
        this.violationRepository = violationRepository;
        this.reportRepository = reportRepository;
    }

    // ===== 기본 기간(최근 30일, 끝날짜 당일 23:59:59.999...) =====
    private static class Range {
        final OffsetDateTime from;
        final OffsetDateTime to;
        Range(OffsetDateTime from, OffsetDateTime to) { this.from = from; this.to = to; }
    }

    private Range resolveRange(OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null) {
            // 시스템 기본 존/오프셋 기준으로 오늘의 시작/끝
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime end = OffsetDateTime.now(zone)
                    .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
            OffsetDateTime start = end.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
            return new Range(start, end);
        }
        return new Range(from, to);
    }

    // ===== 위반 통계 =====
    public ViolationStatistics getViolationStats(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        Range r = resolveRange(from, to);
        List<Violation> mine = violationRepository.findByVideoOwnerIdAndCreatedAtBetween(userId, r.from, r.to);

        int total = mine.size();

        long days = ChronoUnit.DAYS.between(r.from.toLocalDate(), r.to.toLocalDate()) + 1;
        double dailyAvg = days > 0 ? (double) total / days : total;

        // 최근 7일 합계 (to 포함)
        OffsetDateTime weekFrom = r.to.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
        int weeklyDetections = (int) mine.stream()
                .filter(v -> !v.getCreatedAt().isBefore(weekFrom))
                .count();

        // 타입별
        Map<String, Long> byType = mine.stream()
                .collect(Collectors.groupingBy(Violation::getType, Collectors.counting()));

        List<ViolationTypeStatistic> typeStats = byType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new ViolationTypeStatistic(
                        e.getKey(),
                        e.getValue().intValue(),
                        total == 0 ? 0 : (int) Math.round(e.getValue() * 100.0 / total)
                ))
                .toList();

        Map<Integer, Long> byHour = mine.stream()
                .collect(Collectors.groupingBy(v -> v.getCreatedAt().getHour(), Collectors.counting()));

        List<HourlyStatistic> hourly = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            hourly.add(new HourlyStatistic(h, byHour.getOrDefault(h, 0L).intValue()));
        }
        Map<YearMonth, Long> byYm = mine.stream()
                .collect(Collectors.groupingBy(v -> YearMonth.from(v.getCreatedAt()), Collectors.counting()));

        List<YearMonth> months = enumerateMonths(YearMonth.from(r.from), YearMonth.from(r.to));
        List<MonthlyTrend> monthly = new ArrayList<>();
        Integer prev = null;
        for (YearMonth ym : months) {
            int c = byYm.getOrDefault(ym, 0L).intValue();
            int delta = (prev == null) ? 0 : (c - prev);
            monthly.add(new MonthlyTrend(ym.toString(), c, delta)); // "2025-09"
            prev = c;
        }

        List<Report> myReports = reportRepository.findByOwnerIdAndCreatedAtBetween(userId, r.from, r.to);
        int completed = (int) myReports.stream().filter(this::isCompleted).count();
        int detectionAccuracy = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);

        return new ViolationStatistics(
                total,
                detectionAccuracy,
                weeklyDetections,
                round1(dailyAvg),
                typeStats,
                hourly,
                monthly
        );
    }

    // ===== 신고 통계 =====
    public ReportStatistics getReportStats(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        Range r = resolveRange(from, to);
        List<Report> mine = reportRepository.findByOwnerIdAndCreatedAtBetween(userId, r.from, r.to);

        int total = mine.size();
        int completed = (int) mine.stream().filter(this::isCompleted).count();
        int pending   = (int) mine.stream().filter(this::isPending).count();
        int rejected  = (int) mine.stream().filter(this::isRejected).count();

        int successRate = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);

        // 서비스 전체 대비 성공률(옵션)
        List<Report> all = reportRepository.findByCreatedAtBetween(r.from, r.to);
        int totalAll = all.size();
        int completedAll = (int) all.stream().filter(this::isCompleted).count();
        int totalSuccessRate = totalAll == 0 ? 0 : (int) Math.round(completedAll * 100.0 / totalAll);

        List<ReportStatusStatistic> statusStats = List.of(
                new ReportStatusStatistic("처리중", pending),
                new ReportStatusStatistic("완료", completed),
                new ReportStatusStatistic("반려", rejected)
        );

        return new ReportStatistics(
                total,
                completed,
                pending,
                rejected,
                successRate,
                totalSuccessRate,
                statusStats
        );
    }

    // ===== 상태 매핑 (엔티티 상태값에 맞게 조정) =====
    private boolean isCompleted(Report r) {
        String s = r.getStatus().name();
        return "완료".equals(s) || "COMPLETED".equalsIgnoreCase(s) || "APPROVED".equalsIgnoreCase(s);
    }
    private boolean isPending(Report r) {
        String s = r.getStatus().name();
        return "처리중".equals(s) || "PENDING".equalsIgnoreCase(s) || "IN_PROGRESS".equalsIgnoreCase(s);
    }
    private boolean isRejected(Report r) {
        String s = r.getStatus().name();
        return "반려".equals(s) || "REJECTED".equalsIgnoreCase(s);
    }

    // ===== 헬퍼 =====
    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static List<YearMonth> enumerateMonths(YearMonth start, YearMonth end) {
        List<YearMonth> list = new ArrayList<>();
        YearMonth cur = start;
        while (!cur.isAfter(end)) {
            list.add(cur);
            cur = cur.plusMonths(1);
        }
        return list;
    }
}