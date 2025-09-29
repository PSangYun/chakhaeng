package com.ssafy.chakeng.report;

import com.ssafy.chakeng.report.domain.Report;
import com.ssafy.chakeng.report.domain.ReportStatus;
import com.ssafy.chakeng.report.dto.ReportCreateRequest;
import com.ssafy.chakeng.report.dto.ReportResponse;
import com.ssafy.chakeng.report.dto.ReportsResponse;
import com.ssafy.chakeng.video.VideoRepository;
import com.ssafy.chakeng.video.domain.Video;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public Report createFromRequest(ReportCreateRequest req, UUID userId) {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate d = LocalDate.parse(req.getDate());
        LocalTime t = LocalTime.parse(req.getTime());
        OffsetDateTime occurredAt = ZonedDateTime.of(d, t, KST).toOffsetDateTime();

        if (reportRepository.existsByOwnerIdAndPlateNumberAndOccurredAt(userId, req.getPlateNumber(), occurredAt)) {
            throw new IllegalArgumentException("이미 동일 시간/차량으로 신고가 존재합니다.");
        }

        Report report = Report.builder()
                .ownerId(userId)
                .videoId(req.getVideoId())
                .violationType(req.getViolationType())
                .location(req.getLocation())
                .title(req.getTitle())
                .description(req.getDescription())
                .plateNumber(req.getPlateNumber())
                .occurredAt(occurredAt)
                .status(ReportStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        return reportRepository.save(report);
    }

    public List<ReportsResponse> getReportsByUser(UUID ownerId) {
        List<Report> reports = reportRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        return reports.stream()
                .map(r -> ReportsResponse.builder()
                        .id(r.getId())
                        .violationType(r.getViolationType())
                        .location(r.getLocation())
                        .title(r.getTitle())
                        .plateNumber(r.getPlateNumber())
                        .occurredAt(r.getOccurredAt())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public ReportResponse getReportsById(UUID reportId) {
        Optional<Report> report = reportRepository.findById(reportId);
        if (report.isEmpty()) {
            throw new IllegalArgumentException("해당 ID로 신고가 존재하지 않습니다");
        }
        Report r = report.get();
        Video video = videoRepository.findById(r.getVideoId())
                .orElseThrow(() -> new IllegalArgumentException("해당 VIDEO ID로 영상이 존재하지 않습니다."));
        return ReportResponse.builder()
                .id(r.getId())
                .violationType(r.getViolationType())
                .location(r.getLocation())
                .title(r.getTitle())
                .plateNumber(r.getPlateNumber())
                .occurredAt(r.getOccurredAt())
                .status(r.getStatus())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt())
                .objectKey(video.getObjectKey())
                .videoId(video.getId())
                .build();
    }

    @Transactional
    public void cancelReport(UUID ownerId, UUID reportId) {
        Report report = reportRepository.findByIdAndOwnerId(reportId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("신고가 없거나 권한이 없습니다."));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 취소할 수 있습니다.");
        }

        report.setStatus(ReportStatus.CANCELED);
    }

    @Transactional
    public void deleteReport(UUID ownerId, UUID reportId) {
        Report report = reportRepository.findByIdAndOwnerId(reportId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("신고가 없거나 권한이 없습니다."));

        reportRepository.delete(report);
    }
}
