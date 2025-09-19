package com.ssafy.chakeng.violation;

import com.ssafy.chakeng.video.VideoRepository;
import com.ssafy.chakeng.video.domain.Video;
import com.ssafy.chakeng.violation.domain.Violation;
import com.ssafy.chakeng.violation.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViolationService {
    private final ViolationRepository violationRepo;
    private final VideoRepository videoRepository;

    public List<RecentViolationItem> getRecent(UUID ownerId, int limit) {
        Page<Violation> page = violationRepo.findByVideoOwnerIdOrderByCreatedAtDesc(
                ownerId, PageRequest.of(0, Math.max(1, Math.min(limit, 20)))
        );
        return page.getContent().stream().map(v ->
                new RecentViolationItem(
                        v.getId(),
                        v.getType(),
                        toLabel(v.getType()),
                        Optional.ofNullable(v.getPlate()).orElse(""),
                        Optional.ofNullable(v.getLocationText()).orElse(""),
                        v.getCreatedAt()
                )
        ).toList();
    }

    @Transactional
    public ViolationResponse create(UUID ownerId, ViolationCreateRequest req) {
        Video video = videoRepository.findById(req.getVideoId())
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + req.getVideoId()));

        if (!video.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Violation v = new Violation();
        v.setVideo(video);
        v.setType(req.getType());
        v.setPlate(req.getPlate());
        v.setOwner_id(ownerId);
        v.setLocationText(req.getLocationText());
        OffsetDateTime occur = req.getOccurredAt() != null ? req.getOccurredAt() : OffsetDateTime.now();
        v.setCreatedAt(occur);

        Violation saved = violationRepo.save(v);
        return toDto(saved);
    }

    @Transactional
    public List<ViolationResponse> findInRange(UUID ownerId, ViolationRangeQuery q) {
        if (q.getFrom().isAfter(q.getTo())) {
            throw new IllegalArgumentException("from 은 to 보다 빠르거나 같아야 합니다.");
        }
        List<Violation> list = violationRepo.findAllByVideoOwnerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                ownerId, q.getFrom(), q.getTo()
            );
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ViolationDetailResponse getDetail(UUID ownerId, UUID violationId) {
        Violation v = violationRepo.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Violation not found: " + violationId));
        if (!v.getVideo().getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        return toDetailDto(v);
    }

    private ViolationResponse toDto(Violation v) {
        return ViolationResponse.builder()
                .id(v.getId())
                .videoId(v.getVideo().getId())
                .type(v.getType())
                .plate(v.getPlate())
                .locationText(v.getLocationText())
                .occurredAt(v.getCreatedAt())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private ViolationDetailResponse toDetailDto(Violation v) {
        return ViolationDetailResponse.builder()
                .id(v.getId())
                .videoId(v.getVideo().getId())
                .objectKey(v.getVideo().getObjectKey())
                .type(v.getType())
                .plate(v.getPlate())
                .locationText(v.getLocationText())
                .occurredAt(v.getCreatedAt())
                .createdAt(v.getCreatedAt())
                .build();
    }
    private String toLabel(String type) {
        return switch (type) {
            case "SIGNAL" -> "신호위반";
            case "LANE" -> "차선침범";
            case "WRONG_WAY" -> "역주행";
            case "NO_PLATE" -> "무번호판";
            case "NO_HELMET" -> "헬멧 미착용";
            default -> "위반";
        };
    }
}

