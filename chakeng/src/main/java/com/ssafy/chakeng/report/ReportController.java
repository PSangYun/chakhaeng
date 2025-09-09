package com.ssafy.chakeng.report;

import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.report.domain.Report;
import com.ssafy.chakeng.report.dto.ReportCreateRequest;
import com.ssafy.chakeng.report.dto.ReportsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private final String webhookUrl = "https://meeting.ssafy.com/hooks/57qyur84a3nzupbrxxsbghkcjy";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportsResponse>>> getReports(
            @RequestAttribute("userId") UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("조회 성공",reportService.getReportsByUser(userId)));
    }


    @PostMapping("/create-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> send(@RequestBody ReportCreateRequest body, @RequestAttribute("userId") UUID userId) {
        Report saved = reportService.createFromRequest(body,userId);

        String occurredDate = saved.getOccurredAt().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        String occurredTime = saved.getOccurredAt().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String text = String.format(
                "🚨 **교통 법규 위반 신고 알림**\n\n" +
                        "- **위반 유형:** %s\n" +
                        "- **발생 지역:** %s\n" +
                        "- **제목:** %s\n\n" +
                        "**신고 내용**\n%s\n\n" +
                        "- **차량 번호:** %s\n" +
                        "- **발생 일자:** %s\n" +
                        "- **발생 시각:** %s\n" +
                        "- **신고자:** %s\n" +
                        "- **신고 ID:** %s",
                nullToDash(saved.getViolationType()),
                nullToDash(saved.getLocation()),
                nullToDash(saved.getTitle()),
                nullToDash(saved.getDescription()),
                nullToDash(saved.getPlateNumber()),
                occurredDate,
                occurredTime,
                saved.getOwnerId(),
                saved.getId()
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "ReportBot");
        payload.put("icon_emoji", ":rotating_light:");
        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("status", "stored_and_notified");
        return ResponseEntity.ok(ApiResponse.ok("신고완료",resp));
    }

    private static String nullToDash(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    @PostMapping("/{reportId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReport(
            @PathVariable UUID reportId,
            @RequestAttribute("userId") UUID userId
    ) {
        reportService.cancelReport(userId, reportId);
        return ResponseEntity.ok(ApiResponse.ok("신고를 취소했습니다.", null));
    }
}