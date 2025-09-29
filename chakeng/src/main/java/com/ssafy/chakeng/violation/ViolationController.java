package com.ssafy.chakeng.violation;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.violation.dto.ViolationCreateRequest;
import com.ssafy.chakeng.violation.dto.ViolationDetailResponse;
import com.ssafy.chakeng.violation.dto.ViolationRangeQuery;
import com.ssafy.chakeng.violation.dto.ViolationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/violation")
@RequiredArgsConstructor
public class ViolationController {
    private final ViolationService violationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ViolationResponse>> createViolation(
            @Valid @RequestBody ViolationCreateRequest req,
            @RequestAttribute("userId") UUID ownerId
    ) throws FirebaseMessagingException {
        ViolationResponse res = violationService.create(ownerId, req);
        return ResponseEntity.ok(ApiResponse.ok("생성 완료", res));
    }

    @PostMapping("/range")
    public ResponseEntity<ApiResponse<List<ViolationResponse>>> getViolationsInRange(
            @Valid @RequestBody ViolationRangeQuery q,
            @RequestAttribute("userId") UUID ownerId
    ) {
        List<ViolationResponse> list = violationService.findInRange(ownerId, q);
        return ResponseEntity.ok(ApiResponse.ok("조회 완료", list));
    }

    @GetMapping("/{violationId}")
    public ResponseEntity<ApiResponse<ViolationDetailResponse>> getViolation(
            @PathVariable UUID violationId,
            @RequestAttribute("userId") UUID ownerId
    ) {
        ViolationDetailResponse res = violationService.getDetail(ownerId, violationId);
        return ResponseEntity.ok(ApiResponse.ok("상세 조회 완료", res));
    }
}
