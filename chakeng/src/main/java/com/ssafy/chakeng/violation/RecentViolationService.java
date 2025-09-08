package com.ssafy.chakeng.violation;

import com.ssafy.chakeng.violation.domain.Violation;
import com.ssafy.chakeng.violation.dto.RecentViolationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecentViolationService {
    private final ViolationRepository violationRepo;

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

    private String toLabel(String type) {
        return switch (type) {
            case "SIGNAL" -> "신호위반";
            case "LANE" -> "차선침범";
            case "WRONG_WAY" -> "역주행";
            case "NO_PLATE" -> "무번호판";
            default -> "위반";
        };
    }
}

